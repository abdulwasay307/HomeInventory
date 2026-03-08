package com.desktopapp.frontend.constants;


// made all of them static + final. static->accessible without creating object. final->calues cannot be chnaged
public class AppConstants {
    public static final String APP_TITLE = "Desktop App";
    public static final double DEFAULT_WIDTH = 800;
    public static final double DEFAULT_HEIGHT = 600;
    
    // UI Constants
    public static final double SPACING = 10.0;
    public static final double PADDING = 10.0;
    
    // API Constants
    public static final String API_BASE_URL = "http://localhost:5000/api";
    /** WebSocket URL for real-time notifications (backend runs on port 5001). */
    public static final String WS_NOTIFICATIONS_URL = "ws://localhost:5001/notifications";

    // Colors
    public static final String COLOR_PRIMARY = "#1976d2";
    public static final String COLOR_SUCCESS = "#2e7d32";
    public static final String COLOR_WHITE = "white";
    
    // Styles
    public static final String BUTTON_STYLE_PRIMARY = "-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: " + COLOR_WHITE + "; -fx-font-size: 14px; -fx-padding: 8 16; -fx-cursor: hand;";
    public static final String BUTTON_STYLE_SUCCESS = "-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: " + COLOR_WHITE + "; -fx-font-size: 14px; -fx-padding: 8 16; -fx-cursor: hand;";

    // Inventory
    public static final int LOW_STOCK_THRESHOLD = 3;
    public static final int EXPIRY_DAYS_WARNING = 7;
}
