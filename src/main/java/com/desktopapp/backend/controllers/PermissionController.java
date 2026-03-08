package com.desktopapp.backend.controllers;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.utils.JsonUtils;
import com.desktopapp.backend.utils.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionController {

    /**
     * GET all modules from database
     */
    public static HttpHandler getAllModules = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT id, name FROM modules";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> modules = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> module = new HashMap<>();
                module.put("id", rs.getInt("id"));
                module.put("name", rs.getString("name"));
                modules.add(module);
            }

            ResponseUtils.sendJson(exchange, 200, modules);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to fetch modules");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    /**
     * GET permissions for a user. Query param: user_id
     * Returns list of { module_id, module_name, can_read, can_create, can_update, can_delete }
     */
    public static HttpHandler getUserPermissions = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("user_id=")) {
                ResponseUtils.sendError(exchange, 400, "Missing user_id");
                return;
            }
            int userId = Integer.parseInt(query.substring(8).trim());
            conn = DatabaseConfig.getInstance().getConnection();

            // LEFT JOIN gives NULL for p.* when user has no row 
            String sql = """
                SELECT m.id AS module_id, m.name AS module_name,
                    COALESCE(p.can_read, 0) AS can_read,
                    COALESCE(p.can_create, 0) AS can_create,
                    COALESCE(p.can_update, 0) AS can_update,
                    COALESCE(p.can_delete, 0) AS can_delete
                FROM modules m
                LEFT JOIN user_module_permissions p ON p.module_id = m.id AND p.user_id = ?
                ORDER BY m.id
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> permissions = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> perm = new HashMap<>();
                perm.put("module_id", rs.getInt("module_id"));
                perm.put("module_name", rs.getString("module_name"));
                perm.put("can_read", rs.getInt("can_read"));
                perm.put("can_create", rs.getInt("can_create"));
                perm.put("can_update", rs.getInt("can_update"));
                perm.put("can_delete", rs.getInt("can_delete"));
                permissions.add(perm);
            }

            ResponseUtils.sendJson(exchange, 200, permissions);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to fetch user permissions");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    /**
     * PUT - Update user permissions.
     * Body: { "user_id": 1, "permissions": [ { "module_id": 1, "can_read": 1, "can_create": 0, "can_update": 0, "can_delete": 0 }, ... ] }
     */
    public static HttpHandler updateUserPermissions = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());
            if (data.get("user_id") == null) {
                ResponseUtils.sendError(exchange, 400, "Missing user_id");
                return;
            }
            if (data.get("permissions") == null) {
                ResponseUtils.sendError(exchange, 400, "Missing permissions");
                return;
            }

//data.get("user_id") is an Object
// got compile error without cast to umber
            int userId = ((Number) data.get("user_id")).intValue();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> perms = (List<Map<String, Object>>) data.get("permissions");

            conn = DatabaseConfig.getInstance().getConnection();

            String deleteSql = "DELETE FROM user_module_permissions WHERE user_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();



            String insertSql = """
                INSERT INTO user_module_permissions (user_id, module_id, can_read, can_create, can_update, can_delete)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            for (Map<String, Object> p : perms) {
                int moduleId = ((Number) p.get("module_id")).intValue();
                //if value is null set as 0 otherwiuse parse to int becuase by default it is an object
                int canRead = p.get("can_read") == null ? 0 : ((Number) p.get("can_read")).intValue();
                int canCreate = p.get("can_create") == null ? 0 : ((Number) p.get("can_create")).intValue();
                int canUpdate = p.get("can_update") == null ? 0 : ((Number) p.get("can_update")).intValue();
                int canDelete = p.get("can_delete") == null ? 0 : ((Number) p.get("can_delete")).intValue();

                // Only insert if at least one permission is granted
                if (canRead == 0 && canCreate == 0 && canUpdate == 0 && canDelete == 0) {
                    continue;
                }

                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, moduleId);
                insertStmt.setInt(3, canRead);
                insertStmt.setInt(4, canCreate);
                insertStmt.setInt(5, canUpdate);
                insertStmt.setInt(6, canDelete);
                insertStmt.addBatch(); //used to insert multiple rows at once - batch processing
            }
            insertStmt.executeBatch();

            ResponseUtils.sendJson(exchange, 200, Map.of("message", "Permissions updated"));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Failed to update permissions";
            ResponseUtils.sendError(exchange, 500, msg);
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };
}
