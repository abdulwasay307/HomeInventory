package com.desktopapp.backend.routes;

import com.desktopapp.backend.controllers.ItemController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class ItemRoutes {

    public static void setupRoutes(HttpServer server, String basePath) {


         server.createContext(basePath + "/", ItemController.getAllItems);
         server.createContext(basePath + "/add", ItemController.createItem);
         server.createContext(basePath + "/view", ItemController.getItemById);
         server.createContext(basePath + "/edit", ItemController.updateItem);
         server.createContext(basePath + "/remove", ItemController.deleteItem);
    }
}
