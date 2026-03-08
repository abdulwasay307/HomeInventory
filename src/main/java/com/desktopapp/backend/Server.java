//Run backend: mvn compile exec:java -Dexec.mainClass="com.desktopapp.backend.Server"
package com.desktopapp.backend;

import com.desktopapp.backend.config.DatabaseConfig;
import com.desktopapp.backend.routes.AuthRoutes;
import com.desktopapp.backend.routes.UserRoutes;
import com.desktopapp.backend.routes.ItemRoutes;
import com.desktopapp.backend.routes.PermissionRoutes;
import com.desktopapp.backend.routes.ActivityRoutes;
import com.desktopapp.backend.websocket.NotificationWebSocketServer;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {
    private HttpServer server;
    private static final int PORT = 5000;
    private static final int WS_PORT = 5001;

    public void start() throws IOException {
        DatabaseConfig.getInstance();
        server = HttpServer.create(new InetSocketAddress(PORT), 0); // InetSocketAddress tells which IP and PORT to listen
        
        
        AuthRoutes.setupRoutes(server, "/api/auth");
        UserRoutes.setupRoutes(server, "/api/users");
        ItemRoutes.setupRoutes(server, "/api/items");
        PermissionRoutes.setupRoutes(server, "/api/permissions");
        ActivityRoutes.setupRoutes(server, "/api/activity");

        // Start WebSocket server for realtime notifications
        NotificationWebSocketServer.startServer(WS_PORT);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
        NotificationWebSocketServer.stopServer();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop)); //JVM runs addShutdownHooks before exiting and run time lets you interact with the JVM
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}