package Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Server.Request;
import Server.Response;
import Models.User;
import Database.UserRepository;
import Server.AuthToken;
import Server.PasswordHasher;
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

            if (username == null || password == null || username.isBlank() || password.isEmpty()) {
                return new Response(400, "Username and password are required");
            }

            // Same username rules as the token format (letters, digits, '_' and '.')
            if (username.length() > 64 || !username.matches("[A-Za-z0-9_.]+") || password.length() > 128) {
                return new Response(400, "Invalid username or password format");
            }

            if (users.containsKey(username)) {
                return new Response(409, "User already registered");
            }

            // Never store plaintext passwords
            User newUser = new User(username, PasswordHasher.hash(password));
            try {
                userRepository.save(newUser);
                users.put(username, newUser);
                return new Response(201, "User created successfully");
            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState())) { // unique_violation
                    return new Response(409, "User already registered");
                }
                System.err.println("Error creating user: " + e.getMessage());
                return new Response(500, "Error creating user");
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

            if (username == null || password == null) {
                return new Response(401, "Login failed");
            }

            try {
                User user = userRepository.findByUsername(username);
                if (user != null && PasswordHasher.verify(password, user.getPassword())) {
                    // Upgrade legacy plaintext passwords to a hash on successful login
                    if (!PasswordHasher.isHashed(user.getPassword())) {
                        userRepository.updatePassword(username, PasswordHasher.hash(password));
                    }
                    return new Response(200, "User login successful");
                } else {
                    return new Response(401, "Login failed");
                }
            } catch (SQLException e) {
                System.err.println("Error during login: " + e.getMessage());
                return new Response(500, "Error during login");
            }
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    private String extractValue(String json, String key) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(json);
            return node.has(key) ? node.get(key).asText() : null;
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
            System.err.println("Error retrieving user data: " + e.getMessage());
            return new Response(500, "Error retrieving user data");
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
                System.out.println("User not found: " + username);
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
            System.err.println("Error updating user: " + e.getMessage());
            return new Response(500, "Error updating user");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    private boolean isAuthorized(String username, String token) {

        // Users may only access/modify their own profile
        String tokenUser = AuthToken.usernameFrom(token);
        return tokenUser != null && tokenUser.equals(username);
    }

}
