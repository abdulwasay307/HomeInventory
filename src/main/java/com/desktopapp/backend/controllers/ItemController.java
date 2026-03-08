package com.desktopapp.backend.controllers;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.utils.JsonUtils;
import com.desktopapp.backend.utils.ResponseUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ItemController {

    // ===================== GET ALL ITEMS =====================
    public static HttpHandler getAllItems = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT * FROM items";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> items = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("name", rs.getString("name"));
                item.put("description", rs.getString("description"));
                item.put("category_id", rs.getInt("category_id"));
                item.put("room_id", rs.getInt("room_id"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("item_condition", rs.getString("item_condition"));
                item.put("item_brand", rs.getString("item_brand"));
                item.put("model", rs.getString("model"));
                item.put("serial_number", rs.getString("serial_number"));
                Date purchaseDate = rs.getDate("purchase_date");
                item.put("purchase_date", purchaseDate != null ? purchaseDate.toString() : null);
                item.put("purchase_price", rs.getBigDecimal("purchase_price"));
                Date warrantyExpiry = rs.getDate("warranty_expiry");
                item.put("warranty_expiry", warrantyExpiry != null ? warrantyExpiry.toString() : null);
                item.put("image_url", rs.getString("image_url"));
                item.put("notes", rs.getString("notes"));
                item.put("created_at", rs.getTimestamp("created_at"));
                item.put("updated_at", rs.getTimestamp("updated_at"));

                items.add(item);
            }

            ResponseUtils.sendJson(exchange, 200, items);

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to fetch items");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // ===================== GET ITEM BY ID =====================
    public static HttpHandler getItemById = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                ResponseUtils.sendError(exchange, 400, "Missing id"); //HTTP 400 = Bad Request
                return;
            }

            int id = Integer.parseInt(query.substring(3));
            conn = DatabaseConfig.getInstance().getConnection();

            String sql = "SELECT * FROM items WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                ResponseUtils.sendError(exchange, 404, "Item not found");
                return;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", rs.getInt("id"));
            item.put("name", rs.getString("name"));
            item.put("description", rs.getString("description"));
            item.put("category_id", rs.getInt("category_id"));
            item.put("room_id", rs.getInt("room_id"));
            item.put("quantity", rs.getInt("quantity"));
            item.put("item_condition", rs.getString("item_condition"));
            item.put("item_brand", rs.getString("item_brand"));
            item.put("model", rs.getString("model"));
            item.put("serial_number", rs.getString("serial_number"));
            Date purchaseDate = rs.getDate("purchase_date");
            item.put("purchase_date", purchaseDate != null ? purchaseDate.toString() : null);
            item.put("purchase_price", rs.getBigDecimal("purchase_price"));
            Date warrantyExpiry = rs.getDate("warranty_expiry");
            item.put("warranty_expiry", warrantyExpiry != null ? warrantyExpiry.toString() : null);
            item.put("image_url", rs.getString("image_url"));
            item.put("notes", rs.getString("notes"));
            item.put("created_at", rs.getTimestamp("created_at"));
            item.put("updated_at", rs.getTimestamp("updated_at"));

            ResponseUtils.sendJson(exchange, 200, item);

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to fetch item");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // ===================== CREATE ITEM =====================
    public static HttpHandler createItem = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());
            conn = DatabaseConfig.getInstance().getConnection();

            String sql = """
                INSERT INTO items
                (name, description, category_id, room_id, quantity, item_condition,
                 item_brand, model, serial_number, purchase_date, purchase_price,
                 warranty_expiry, image_url, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) data.get("name"));
            stmt.setString(2, (String) data.get("description"));
            stmt.setInt(3, ((Number) data.get("category_id")).intValue());
            stmt.setInt(4, ((Number) data.get("room_id")).intValue());
            stmt.setInt(5, ((Number) data.get("quantity")).intValue());
            stmt.setString(6, (String) data.get("item_condition"));
            stmt.setString(7, (String) data.get("item_brand"));
            stmt.setString(8, (String) data.get("model"));
            stmt.setString(9, (String) data.get("serial_number"));
            stmt.setDate(10, Date.valueOf((String) data.get("purchase_date")));
            stmt.setBigDecimal(11, new BigDecimal(data.get("purchase_price").toString()));
            stmt.setDate(12, Date.valueOf((String) data.get("warranty_expiry")));
            stmt.setString(13, (String) data.get("image_url"));
            stmt.setString(14, (String) data.get("notes"));

            stmt.executeUpdate();
            ResponseUtils.sendJson(exchange, 201, Map.of("message", "Item created"));

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            ResponseUtils.sendError(exchange, 500, "Failed to create item: " + errorMsg);
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // ===================== UPDATE ITEM =====================
    public static HttpHandler updateItem = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());
            
            if (data.get("id") == null) {
                ResponseUtils.sendError(exchange, 400, "Missing required field: id");
                return;
            }
            
            conn = DatabaseConfig.getInstance().getConnection();

            String sql = """
                UPDATE items SET
                name=?, description=?, category_id=?, room_id=?, quantity=?,
                item_condition=?, item_brand=?, model=?, serial_number=?,
                purchase_date=?, purchase_price=?, warranty_expiry=?,
                image_url=?, notes=?, updated_at=NOW()
                WHERE id=?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, (String) data.get("name"));
            stmt.setString(2, (String) data.get("description"));
            stmt.setInt(3, ((Number) data.get("category_id")).intValue());
            stmt.setInt(4, ((Number) data.get("room_id")).intValue());
            stmt.setInt(5, ((Number) data.get("quantity")).intValue());
            stmt.setString(6, (String) data.get("item_condition"));
            stmt.setString(7, (String) data.get("item_brand"));
            stmt.setString(8, (String) data.get("model"));
            stmt.setString(9, (String) data.get("serial_number"));
            stmt.setDate(10, Date.valueOf((String) data.get("purchase_date")));
            stmt.setBigDecimal(11, new BigDecimal(data.get("purchase_price").toString()));
            stmt.setDate(12, Date.valueOf((String) data.get("warranty_expiry")));
            stmt.setString(13, (String) data.get("image_url"));
            stmt.setString(14, (String) data.get("notes"));
            stmt.setInt(15, ((Number) data.get("id")).intValue());

            stmt.executeUpdate();
            ResponseUtils.sendJson(exchange, 200, Map.of("message", "Item updated"));

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            ResponseUtils.sendError(exchange, 500, "Failed to update item: " + errorMsg);
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };

    // ===================== DELETE ITEM =====================
    public static HttpHandler deleteItem = (HttpExchange exchange) -> {
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Connection conn = null;
        try {
            Map<String, Object> data = JsonUtils.fromJsonToMap(exchange.getRequestBody());
            conn = DatabaseConfig.getInstance().getConnection();

            String sql = "DELETE FROM items WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ((Number) data.get("id")).intValue());

            stmt.executeUpdate();
            ResponseUtils.sendJson(exchange, 200, Map.of("message", "Item deleted"));

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.sendError(exchange, 500, "Failed to delete item");
        } finally {
            DatabaseConfig.getInstance().closeConnection(conn);
        }
    };
}
