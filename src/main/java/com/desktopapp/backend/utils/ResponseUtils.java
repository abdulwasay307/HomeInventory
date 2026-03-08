package com.desktopapp.backend.utils;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ResponseUtils {

   
     // Sends a JSON response with the specified data 
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtils.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, Map.of("message", message));
    }
}

