package com.desktopapp.backend.routes;

import com.desktopapp.backend.controllers.UserController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class UserRoutes {

    public static void setupRoutes(HttpServer server, String basePath) {
        server.createContext(basePath + "/",  UserController.view);
        server.createContext(basePath + "/add",  UserController.create);
        server.createContext(basePath + "/remove",  UserController.delete);
        server.createContext(basePath + "/edit",  UserController.update);

    }
}