package com.desktopapp.backend.routes;

import com.desktopapp.backend.controllers.AuthController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class AuthRoutes {


    // if login,logout,register functions were not static, we would have to create an instance of AuthController class to access those functions



    public static void setupRoutes(HttpServer server, String basePath) {
        server.createContext(basePath + "/login",  AuthController.login);
        server.createContext(basePath + "/logout",  AuthController.logout);
        server.createContext(basePath + "/register",  AuthController.register);
    }
}