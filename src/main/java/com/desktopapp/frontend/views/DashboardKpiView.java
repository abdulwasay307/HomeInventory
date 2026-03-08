package com.desktopapp.frontend.views;

import com.desktopapp.frontend.constants.AppConstants;
import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.services.APIService;
import com.desktopapp.frontend.services.NotificationWebSocketClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.util.List;
import java.util.Map;


public class DashboardKpiView extends VBox {

    private static final int RECENT_UPDATES_LIMIT = 4;

    private final Label totalUsersLabel;
    private final Label totalItemsLabel;
    private final Label lowStockLabel;
    private final VBox recentUpdatesBox;

    private NotificationWebSocketClient notificationWebSocketClient;
    private volatile boolean disposing;

    public DashboardKpiView() {
        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #1a1a1a;");

        Label header = new Label("Key metrics");
        header.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 20px; -fx-font-weight: bold;");

        totalUsersLabel = createKpiPlaceholder();
        totalItemsLabel = createKpiPlaceholder();
        lowStockLabel = createKpiPlaceholder();

        VBox card1 = createKpiCard("Total Users", totalUsersLabel);
        VBox card2 = createKpiCard("Total Items", totalItemsLabel);
        VBox card3 = createKpiCard("Low Stock Items", lowStockLabel);

        HBox kpiRow = new HBox(20);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.getChildren().addAll(card1, card2, card3);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #404040; -fx-text-fill: #e0e0e0; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshRecentUpdates());

        HBox recentHeaderRow = new HBox(10);
        recentHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Label recentHeader = new Label("Recent Updates");
        recentHeader.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 18px; -fx-font-weight: bold;");
        recentHeaderRow.getChildren().addAll(recentHeader, refreshBtn);

        recentUpdatesBox = new VBox(8);
        recentUpdatesBox.setPadding(new Insets(0));

        getChildren().addAll(header, kpiRow, recentHeaderRow, recentUpdatesBox);
        loadKpis();
        loadRecentUpdates();
        initNotificationWebSocket();
    }

    /** Call when Dashboard tab is shown: reconnect WebSocket if not connected. */
    public void ensureWebSocketConnected() {
        if (disposing) return;
        if (notificationWebSocketClient == null || !notificationWebSocketClient.isOpen()) {
            initNotificationWebSocket();
        }
    }

    /** Clean up WebSocket when view is no longer used. */
    public void dispose() {
        disposing = true;
        if (notificationWebSocketClient != null) {
            try {
                notificationWebSocketClient.close();
            } catch (Exception ignored) {
            }
            notificationWebSocketClient = null;
        }
    }

    public void refreshRecentUpdates() {
        loadRecentUpdates();
    }

    /** Refresh KPIs and recent updates  */
    public void refresh() {
        loadKpis();
        loadRecentUpdates();
    }

    private static Label createKpiPlaceholder() {
        Label l = new Label("—");
        l.setStyle("-fx-text-fill: #fff; -fx-font-size: 28px; -fx-font-weight: bold;");
        return l;
    }

    private static VBox createKpiCard(String title, Label valueLabel) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setMinWidth(120);
        card.setStyle(
            "-fx-background-color: #2d2d2d; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #404040; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1;"
        );
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 14px;");
        valueLabel.setStyle("-fx-text-fill: #fff; -fx-font-size: 28px; -fx-font-weight: bold;");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void initNotificationWebSocket() {
        if (disposing) return;
        if (notificationWebSocketClient != null && notificationWebSocketClient.isOpen()) return;
        if (notificationWebSocketClient != null) {
            try { notificationWebSocketClient.close(); } catch (Exception ignored) { }
            notificationWebSocketClient = null;
        }
        try {
            URI uri = new URI(AppConstants.WS_NOTIFICATIONS_URL);
            notificationWebSocketClient = new NotificationWebSocketClient(
                uri,
                message -> Platform.runLater(this::refresh)
            );
            Thread connectThread = new Thread(() -> {
                try {
                    notificationWebSocketClient.connect();
                } catch (Exception e) {
                    if (!disposing) {
                        System.err.println("Notification WebSocket connect failed: " + e.getMessage());
                    }
                }
            }, "ws-connect");
            connectThread.setDaemon(true);
            connectThread.start();
        } catch (Exception e) {
            System.err.println("Failed to initialize notification WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadKpis() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> users = APIService.getInstance().getAllUsers();
                List<Map<String, Object>> items = APIService.getInstance().getAllItems();

                int lowStock = 0;
                int threshold = AppConstants.LOW_STOCK_THRESHOLD;
                for (Map<String, Object> item : items) {
                    Object q = item.get("quantity");
                    int qty = q instanceof Number ? ((Number) q).intValue() : 0;
                    if (qty >= 0 && qty <= threshold) lowStock++;
                }

                final int totalUsers = users.size();
                final int totalItems = items.size();
                final int lowStockCount = lowStock;

                Platform.runLater(() -> {
                    totalUsersLabel.setText(String.valueOf(totalUsers));
                    totalItemsLabel.setText(String.valueOf(totalItems));
                    lowStockLabel.setText(String.valueOf(lowStockCount));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    totalUsersLabel.setText("—");
                    totalItemsLabel.setText("—");
                    lowStockLabel.setText("—");
                });
            }
        }).start();
    }

    private void loadRecentUpdates() {
        new Thread(() -> {
            try {
                String forRole = AuthManager.getInstance().isAdmin() ? "admin" : "user";
                List<Map<String, Object>> list = APIService.getInstance().getRecentActivity(forRole);
                Platform.runLater(() -> {
                    recentUpdatesBox.getChildren().clear();
                    if (list == null || list.isEmpty()) {
                        Label empty = new Label("No recent updates.");
                        empty.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 14px;");
                        recentUpdatesBox.getChildren().add(empty);
                    } else {
                        int count = Math.min(RECENT_UPDATES_LIMIT, list.size());
                        for (int i = 0; i < count; i++) {
                            Map<String, Object> row = list.get(i);
                            recentUpdatesBox.getChildren().add(buildUpdateCard(row));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    recentUpdatesBox.getChildren().clear();
                    Label err = new Label("Could not load recent updates.");
                    err.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 14px;");
                    recentUpdatesBox.getChildren().add(err);
                });
            }
        }).start();
    }

    private HBox buildUpdateCard(Map<String, Object> row) {
        String desc = (String) row.get("description");
        Object created = row.get("created_at");
        String timeStr = formatTimestamp(created);
        if (desc == null) desc = "";

        String who = (String) row.get("user_name");
        if (who == null || who.isEmpty()) who = (String) row.get("user_email");
        String leftText = desc;
        if (who != null && !who.isEmpty()) leftText = desc + " — " + who;

        Label leftLabel = new Label(leftText);
        leftLabel.setMaxWidth(Double.MAX_VALUE);
        leftLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px;");
        leftLabel.setWrapText(false);
        leftLabel.setEllipsisString("…");

        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12px;");

        HBox card = new HBox(12, leftLabel, timeLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        HBox.setHgrow(leftLabel, Priority.ALWAYS);
        card.setStyle(
            "-fx-background-color: #2d2d2d; " +
            "-fx-background-radius: 6; " +
            "-fx-border-color: #404040; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 1;"
        );
        return card;
    }

    private static String formatTimestamp(Object created) {
        if (created == null) return "";
        String s = created.toString();
        try {
            if (s.length() >= 19) {
                String datePart = s.substring(0, 10);
                String timePart = s.substring(11, 19);
                return datePart + " " + timePart;
            }
        } catch (Exception ignored) { }
        return s;
    }
}
