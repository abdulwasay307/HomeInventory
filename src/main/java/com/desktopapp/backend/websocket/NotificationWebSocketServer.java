package com.desktopapp.backend.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class NotificationWebSocketServer extends WebSocketServer {

    private static NotificationWebSocketServer instance;

    public static synchronized NotificationWebSocketServer startServer(int port) {
        if (instance == null) {
            instance = new NotificationWebSocketServer(new InetSocketAddress(port));
            instance.start();
            System.out.println("Notification WebSocket server started on port " + port);
        }
        return instance;
    }

    public static synchronized void stopServer() {
        if (instance != null) {
            try {
                instance.stop();
                System.out.println("Notification WebSocket server stopped");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                instance = null;
            }
        }
    }

    public static synchronized void broadcastNotification(String message) {
        if (instance != null) {
            instance.broadcast(message != null ? message : "activity_update");
        }
    }

    private NotificationWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Notification WebSocket client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Notification WebSocket client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Currently we don't expect messages from clients; just log them
        System.out.println("Notification WebSocket received message from client: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Notification WebSocket server error: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Notification WebSocket server started successfully");
    }
}

