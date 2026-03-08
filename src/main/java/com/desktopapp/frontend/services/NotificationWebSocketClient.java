package com.desktopapp.frontend.services;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class NotificationWebSocketClient extends WebSocketClient {

    public interface Listener {
        void onNotificationReceived(String message);
    }

    private final Listener listener;

    public NotificationWebSocketClient(URI serverUri, Listener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Notification WebSocket opened: " + getURI());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Notification WebSocket message: " + message);
        if (listener != null) {
            listener.onNotificationReceived(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Notification WebSocket closed. Code=" + code + ", reason=" + reason + ", remote=" + remote);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Notification WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }
}
