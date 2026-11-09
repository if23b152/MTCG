package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final ServerSocket serverSocket;
    private final int port = 10001;
    private final Router router;
    private final ExecutorService threadPool;

    // Limits against oversized / slow requests (DoS)
    private static final int MAX_BODY_CHARS = 1_000_000;
    private static final int MAX_HEADERS = 100;
    private static final int SOCKET_TIMEOUT_MS = 10_000;

    public HttpServer() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.router = new Router();
        new RouterConfig(router);
        this.threadPool = Executors.newFixedThreadPool(10); // 10 gleichzeitige threds
    }

    public void start() {
        System.out.println("Server started on port " + port);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); //waartet auf clients
                threadPool.execute(() -> handleClient(clientSocket)); // multithreading aktiv
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            Response response;
            try {
                Request request = parseRequest(reader);
                response = processRequest(request);
            } catch (IllegalArgumentException e) {
                response = new Response(400, "Bad Request");
            }

            writer.write(response.toString());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Request parseRequest(BufferedReader reader) throws IOException {
        Request request = new Request();

        String requestLine = reader.readLine();
        if (requestLine != null) {
            String[] requestLineParts = requestLine.split(" ");
            if (requestLineParts.length >= 2) {
                request.setMethod(requestLineParts[0]);
                String[] pathAndQuery = requestLineParts[1].split("\\?");
                request.setPath(pathAndQuery[0]);
                if (pathAndQuery.length > 1) {
                    request.setQueryParams(pathAndQuery[1]);
                }
            }
        }

        String headerLine;
        int headerCount = 0;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            if (++headerCount > MAX_HEADERS) {
                throw new IllegalArgumentException("Too many headers");
            }
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                request.getHeaders().put(headerParts[0], headerParts[1]);
            }
        }

        if (request.getHeaders().containsKey("Content-Length")) {
            int contentLength;
            try {
                contentLength = Integer.parseInt(request.getHeaders().get("Content-Length").trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }
            // Reject negative or huge values (previously: allocation of arbitrary size -> OOM)
            if (contentLength < 0 || contentLength > MAX_BODY_CHARS) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }
            char[] bodyChars = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int n = reader.read(bodyChars, read, contentLength - read);
                if (n == -1) {
                    break;
                }
                read += n;
            }
            request.setBody(new String(bodyChars, 0, read));
        }

        return request;
    }

    private Response processRequest(Request request) {
        try {
            return router.handleRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "Internal Server Error");
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                threadPool.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
