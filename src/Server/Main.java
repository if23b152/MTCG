import java.util.UUID; // für die Token-Erstellung
import java.util.HashMap; // für die Speicherung von Tokens
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainServerApp {
    static Map<String, String> userTokens = new HashMap<>(); // Speichert Benutzer und Tokens
    static Map<String, String> userPasswords = new HashMap<>(); // Speichert Benutzer und Passwörter

    public static void main(String[] args) throws IOException {
        int serverPort = 10001; // server port
        HttpServer myServer = HttpServer.create(new InetSocketAddress(serverPort), 0); // server

        myServer.createContext("/users", new CreateUserEndpoint()); // Endpoint für Benutzerregistrierung
        myServer.createContext("/sessions", new LoginEndpoint()); // Endpoint für Login
        myServer.setExecutor(null); // threads

        System.out.println("Server startet auf Port " + serverPort); // Info, dass der Server läuft
        myServer.start(); // Start des Servers
    }

    // Registrierung eines neuen Benutzers
    static class CreateUserEndpoint implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Methode nicht erlaubt
                return;
            }

            InputStream reqStream = exchange.getRequestBody();
            byte[] requestBody = reqStream.readAllBytes();
            String jsonBody = new String(requestBody, StandardCharsets.UTF_8);

            ObjectMapper objMapper = new ObjectMapper();
            Map<String, String> userData = objMapper.readValue(jsonBody, HashMap.class);

            String user = userData.get("Username");
            String pass = userData.get("Password");

            // Überprüfen, ob der Benutzer bereits existiert
            if (userPasswords.containsKey(user)) {
                String response = "Benutzer existiert bereits!";
                exchange.sendResponseHeaders(409, response.length()); // Konflikt
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            userPasswords.put(user, pass); // Benutzer und Passwort speichern
            userTokens.put(user, null); // Initial kein Token für den Benutzer

            String responseMessage = "Benutzer " + user + " wurde erfolgreich registriert!";
            System.out.println("Neuer Benutzer: " + user);

            exchange.sendResponseHeaders(201, responseMessage.length()); // Erfolgreich erstellt
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseMessage.getBytes());
            outputStream.close();
        }
    }

    // Login-Handler
    static class LoginEndpoint implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Methode nicht erlaubt
                return;
            }

            InputStream reqStream = exchange.getRequestBody();
            byte[] requestBody = reqStream.readAllBytes();
            String jsonBody = new String(requestBody, StandardCharsets.UTF_8);

            ObjectMapper objMapper = new ObjectMapper();
            Map<String, String> userData = objMapper.readValue(jsonBody, HashMap.class);

            String user = userData.get("Username");
            String pass = userData.get("Password");

            // Überprüfen, ob der Benutzer existiert und das Passwort korrekt ist
            if (!userPasswords.containsKey(user) || !userPasswords.get(user).equals(pass)) {
                String response = "Benutzername oder Passwort ist falsch!";
                exchange.sendResponseHeaders(401, response.length()); // Nicht autorisiert
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Generiere Token und speichere es
            String token = UUID.randomUUID().toString();
            userTokens.put(user, token);

            String responseMessage = "Login erfolgreich! Token: " + token;
            exchange.sendResponseHeaders(200, responseMessage.length()); // OK
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseMessage.getBytes());
            outputStream.close();
        }
    }
}
