// src/main/java/server/Response.java
package main.Server;
import java.util.HashMap;
import java.util.Map;


public class Response {
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        headers.put("Content-Type", "application/json");
    }

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(getStatusMessage()).append("\r\n");
        headers.forEach((key, value) -> response.append(key).append(": ").append(value).append("\r\n"));
        response.append("\r\n");
        if (body != null) {
            response.append(body);
        }
        return response.toString();
    }

    private String getStatusMessage() {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}
