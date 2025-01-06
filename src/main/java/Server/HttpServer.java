package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private final ServerSocket serverSocket;
    private final int port = 10001;
    private final Router router;

    public HttpServer() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.router = new Router();
        new RouterConfig(router);
    }

    public void start() {
        try {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            Request request = parseRequest(reader);
            Response response = processRequest(request);

            writer.write(response.toString());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
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
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                request.getHeaders().put(headerParts[0], headerParts[1]);
            }
        }

        if (request.getHeaders().containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(request.getHeaders().get("Content-Length"));
            char[] bodyChars = new char[contentLength];
            reader.read(bodyChars, 0, contentLength);
            request.setBody(new String(bodyChars));
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
