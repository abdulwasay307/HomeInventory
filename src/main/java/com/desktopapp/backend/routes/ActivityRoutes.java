package com.desktopapp.backend.routes;

import com.desktopapp.backend.controllers.ActivityLogController;
import com.sun.net.httpserver.HttpServer;

public class ActivityRoutes {

    public static void setupRoutes(HttpServer server, String basePath) {
        server.createContext(basePath + "/", ActivityLogController.getRecent);
        server.createContext(basePath + "/add", ActivityLogController.log);
    }
}
