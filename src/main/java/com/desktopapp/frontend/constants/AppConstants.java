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

    // Inventory
    public static final int LOW_STOCK_THRESHOLD = 3;
    public static final int EXPIRY_DAYS_WARNING = 7;
}
