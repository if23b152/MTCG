import Server.HttpServer;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = new HttpServer();
            System.out.println("Starting server...");
            server.start();
        } catch (IOException e) {
            System.out.println("Server failed to start: " + e.getMessage());
        }
    }
}
