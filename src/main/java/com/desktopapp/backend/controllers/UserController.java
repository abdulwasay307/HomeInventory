package com.desktopapp.backend.controllers;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.models.User;
import com.desktopapp.backend.utils.JsonUtils;
import com.desktopapp.backend.utils.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class UserController {

    // GET ALL USERS
    public static HttpHandler view = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1); //405=method not allowedd
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT id, first_name, last_name, email, role FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> users = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("first_name", rs.getString("first_name"));
                user.put("last_name", rs.getString("last_name"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                users.add(user);
            }

            ResponseUtils.sendJson(exchange, 200, users);

        } catch (Exception e) {
            ResponseUtils.sendError(exchange, 500, "Failed to fetch users");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // CREATE USER
    public static HttpHandler create = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            InputStream body = exchange.getRequestBody();
            Map<String, Object> data = JsonUtils.fromJsonToMap(body);

            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";

            String password = (String) data.get("password");
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) data.get("first_name"));
            stmt.setString(2, (String) data.get("last_name"));
            stmt.setString(3, (String) data.get("email"));
            stmt.setString(4, hashedPassword); 
            stmt.setString(5, "user");

            stmt.executeUpdate();
            ResponseUtils.sendJson(exchange, 201, Map.of("message", "User created"));

        } catch (Exception e) {
            ResponseUtils.sendError(exchange, 500, "Failed to create user");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // UPDATE USER
    public static HttpHandler update = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());

            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "UPDATE users SET first_name=?, last_name=?, role=? WHERE id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) data.get("first_name"));
            stmt.setString(2, (String) data.get("last_name"));
            stmt.setString(3, (String) data.get("role"));
            stmt.setInt(4, ((Number) data.get("id")).intValue());

            stmt.executeUpdate();
            ResponseUtils.sendJson(exchange, 200, Map.of("message", "User updated"));

        } catch (Exception e) {
            ResponseUtils.sendError(exchange, 500, "Failed to update user");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // DELETE USER
    public static HttpHandler delete = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());

            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "DELETE FROM users WHERE id=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ((Number) data.get("id")).intValue());
            stmt.executeUpdate();

            ResponseUtils.sendJson(exchange, 200, Map.of("message", "User deleted"));

        } catch (Exception e) {
            ResponseUtils.sendError(exchange, 500, "Failed to delete user");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

}
