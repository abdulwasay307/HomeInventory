package com.desktopapp.backend.routes;

import com.desktopapp.backend.controllers.PermissionController;
import com.sun.net.httpserver.HttpServer;

public class PermissionRoutes {

    public static void setupRoutes(HttpServer server, String basePath) {
        server.createContext(basePath + "/modules", PermissionController.getAllModules);
        server.createContext(basePath + "/user", (exchange) -> {
            String method = exchange.getRequestMethod();
            if (method.equalsIgnoreCase("GET")) {
                PermissionController.getUserPermissions.handle(exchange);
            } else if (method.equalsIgnoreCase("PUT")) {
                PermissionController.updateUserPermissions.handle(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });
    }
}
