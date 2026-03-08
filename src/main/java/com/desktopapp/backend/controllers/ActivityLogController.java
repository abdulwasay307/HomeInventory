package com.desktopapp.backend.controllers;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.utils.JsonUtils;
import com.desktopapp.backend.utils.ResponseUtils;
import com.desktopapp.backend.websocket.NotificationWebSocketServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActivityLogController {

    private static final int RECENT_LIMIT = 50;

    /** POST */
    public static HttpHandler log = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());
            if (data.get("user_id") == null || data.get("description") == null || data.get("type") == null) {
                ResponseUtils.sendError(exchange, 400, "Missing user_id, description, or type");
                return;
            }
            int userId = ((Number) data.get("user_id")).intValue();
            String description = (String) data.get("description");
            String type = (String) data.get("type");
            if (!type.matches("admin|users|both")) {
                ResponseUtils.sendError(exchange, 400, "type must be admin, users, or both");
                return;
            }
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "INSERT INTO activity_log (user_id, description, type) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, description);
            stmt.setString(3, type);
            stmt.executeUpdate();

            NotificationWebSocketServer.broadcastNotification("activity_update");

            ResponseUtils.sendJson(exchange, 201, Map.of("message", "Logged"));
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to log activity");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    /** GET */
    public static HttpHandler getRecent = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Connection conn = null;
        try {
            String query = exchange.getRequestURI().getQuery();
            String forRole = "both";
            if (query != null && query.startsWith("for_role=")) {
                forRole = query.substring(9).trim().toLowerCase();
                if (!forRole.equals("admin") && !forRole.equals("user")) forRole = "both";
            }
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT al.id, al.user_id, al.description, al.type, al.created_at, u.first_name, u.last_name, u.email " +
                "FROM activity_log al " +
                "LEFT JOIN users u ON u.id = al.user_id " +
                "WHERE al.type = ? OR al.type = 'both' " +
                "ORDER BY al.created_at DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, forRole);
            stmt.setInt(2, RECENT_LIMIT);
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("user_id", rs.getInt("user_id"));
                row.put("description", rs.getString("description"));
                row.put("type", rs.getString("type"));
                Object ts = rs.getTimestamp("created_at");
                row.put("created_at", ts != null ? ts.toString() : null);
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String email = rs.getString("email");
                String userName = (first != null ? first : "").trim() + " " + (last != null ? last : "").trim();
                userName = userName.trim();
                if (userName.isEmpty() && email != null) userName = email;
                row.put("user_name", userName);
                list.add(row);
            }
            ResponseUtils.sendJson(exchange, 200, list);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to fetch activity log");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };
}
