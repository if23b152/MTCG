package main.Controller;

import main.Server.Request;
import main.Server.Response;
import main.Models.User;
import java.util.HashMap;
import java.util.Map;

public class UserController {
    private final Map<String, User> users = new HashMap<>();

    public Response createUser(Request request) {
        try {
            String body = request.getBody();
            String username = extractValue(body, "Username");
            String password = extractValue(body, "Password");

            if (username == null || password == null) {
                return new Response(400, "Username and password are required");
            }

            if (users.containsKey(username)) {
                return new Response(409, "User with same username already registered");
            }

            User newUser = new User(username, password);
            users.put(username, newUser);
            return new Response(201, "User successfully created");

        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    public Response login(Request request) {
        try {
            String body = request.getBody();
            String username = extractValue(body, "Username");
            String password = extractValue(body, "Password");

            User user = users.get(username);
            if (user != null && user.getPassword().equals(password)) {
                String token = username + "-mtcgToken";
                return new Response(200, token);
            }

            return new Response(401, "Invalid username/password provided");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    public Response getUserData(Request request, String username) {
        String token = request.getHeaders().get("Authorization");
        if (!isAuthorized(username, token)) {
            return new Response(401, "Access token is missing or invalid");
        }

        User user = users.get(username);
        if (user == null) {
            return new Response(404, "User not found");
        }

        return new Response(200, user.toJson());
    }

    public Response updateUser(Request request, String username) {
        String token = request.getHeaders().get("Authorization");
        if (!isAuthorized(username, token)) {
            return new Response(401, "Access token is missing or invalid");
        }

        User user = users.get(username);
        if (user == null) {
            return new Response(404, "User not found");
        }

        try {
            String body = request.getBody();
            String name = extractValue(body, "Name");
            String bio = extractValue(body, "Bio");
            String image = extractValue(body, "Image");

            user.updateProfile(name, bio, image);
            return new Response(200, "User successfully updated");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    private boolean isAuthorized(String username, String token) {
        return token != null && token.equals("Bearer " + username + "-mtcgToken");
    }

    private String extractValue(String json, String key) {
        int startIndex = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
        int endIndex = json.indexOf("\"", startIndex);
        return json.substring(startIndex, endIndex);
    }
}
