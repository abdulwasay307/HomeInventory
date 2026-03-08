package com.desktopapp.backend.controllers;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.models.User;
import com.desktopapp.backend.services.JWTService;
import com.desktopapp.backend.utils.JsonUtils;
import com.desktopapp.backend.utils.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AuthController {


    public static HttpHandler login = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1); //HTTP Error 405 = Method Not Allowed
            return;
        }
        Connection connection = null;
        try {
            InputStream requestBody = exchange.getRequestBody();
            Map<String, Object> loginData = JsonUtils.fromJsonToMap(requestBody);
            String email = (String) loginData.get("email");
            String password = (String) loginData.get("password");

            connection = DatabaseConfig.getInstance().getConnection();
            String query = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                ResponseUtils.sendError(exchange, 401, "Invalid credentials");
                return;
            }

            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));

            String storedPassword = user.getPassword();
            if (storedPassword == null || storedPassword.isEmpty()) {
                ResponseUtils.sendError(exchange, 500, "User account error");
                return;
            }

            boolean passwordMatches = BCrypt.checkpw(password, storedPassword);
            if (!passwordMatches) {
                ResponseUtils.sendError(exchange, 401, "Invalid credentials");
                return;
            }

            Map<String, Object> tokenPayload = new HashMap<>();
            tokenPayload.put("id", user.getId());
            tokenPayload.put("email", user.getEmail());
            tokenPayload.put("role", user.getRole());

            String token = JWTService.generateToken(tokenPayload);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("email", user.getEmail());
            userResponse.put("first_name", user.getFirstName());
            userResponse.put("last_name", user.getLastName());
            userResponse.put("role", user.getRole());

            response.put("user", userResponse);
            ResponseUtils.sendJson(exchange, 200, response);

        } catch (Exception e) {
            String errorMsg = e.getMessage() == null ? "Server error: " + e.getClass().getSimpleName() : "Server error: " + e.getMessage();
            ResponseUtils.sendError(exchange, 500, errorMsg);
        } finally {
            if (connection != null) {
                DatabaseConfig.getInstance().closeConnection(connection);
            }
        }
    };

    public static HttpHandler logout = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        ResponseUtils.sendJson(exchange, 200, response);
    };

    public static HttpHandler register = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Connection connection = null;
        try {
            InputStream requestBody = exchange.getRequestBody();
            Map<String, Object> userData = JsonUtils.fromJsonToMap(requestBody);

            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String email = (String) userData.get("email");
            String password = (String) userData.get("password");

            if (firstName == null || lastName == null || email == null || password == null ||
                firstName.trim().isEmpty() || lastName.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                ResponseUtils.sendError(exchange, 400, "All fields are required"); //400= bad request
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
            connection = DatabaseConfig.getInstance().getConnection();

            String query = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, "user");
            stmt.executeUpdate();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            ResponseUtils.sendJson(exchange, 201, response);

        } catch (Exception e) {
            ResponseUtils.sendError(exchange, 500, "Server error");
        } finally {
            if (connection != null) {
                DatabaseConfig.getInstance().closeConnection(connection);
            }
        }
    };

}
