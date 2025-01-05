package main.Controller;

import main.Server.Request;
import main.Server.Response;
import main.Models.User;
import main.Database.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

public class UserController {
    private final Map<String, User> users = new HashMap<>();
    private final UserRepository userRepository;

    public UserController() {
        this.userRepository = new UserRepository();
    }

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
            try {
                userRepository.save(newUser);
                users.put(username, newUser);
                return new Response(201, "User created successfully");
            } catch (SQLException e) {
                return new Response(500, "Error creating user: " + e.getMessage());
            }
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    public Response login(Request request) {
        try {
            String body = request.getBody();
            String username = extractValue(body, "Username");
            String password = extractValue(body, "Password");

            try {
                User user = userRepository.findByUsername(username);
                if (user != null && user.getPassword().equals(password)) {
                    return new Response(200, "User login successful");
                } else {
                    return new Response(401, "Invalid username or password");
                }
            } catch (SQLException e) {
                return new Response(500, "Error during login: " + e.getMessage());
            }
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    private String extractValue(String json, String key) {
        try {
            int startIndex = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
            int endIndex = json.indexOf("\"", startIndex);
            if (startIndex < 0 || endIndex < 0) {
                return null;
            }
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public Response getUserData(Request request, String username) {
        String token = request.getHeaders().get("Authorization");
        if (!isAuthorized(username, token)) {
            return new Response(401, "Access token is missing or invalid");
        }

        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return new Response(404, "User not found");
            }
            return new Response(200, user.toJson());
        } catch (SQLException e) {
            return new Response(500, "Error retrieving user data: " + e.getMessage());
        }
    }

    public Response updateUser(Request request, String username) {
        String token = request.getHeaders().get("Authorization");
        if (!isAuthorized(username, token)) {
            return new Response(401, "Access token is missing or invalid");
        }

        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return new Response(404, "User not found");
            }

            String body = request.getBody();
            String name = extractValue(body, "Name");
            String bio = extractValue(body, "Bio");
            String image = extractValue(body, "Image");

            user.updateProfile(name, bio, image);
            userRepository.update(user);
            return new Response(200, "User successfully updated");
        } catch (SQLException e) {
            return new Response(500, "Error updating user: " + e.getMessage());
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    private boolean isAuthorized(String username, String token) {
        return token != null && token.equals("Bearer " + username + "-mtcgToken");
    }
}
