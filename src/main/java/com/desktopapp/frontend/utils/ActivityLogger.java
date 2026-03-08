package com.desktopapp.frontend.utils;

import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.services.APIService;

/**
 * Logs activity for Recent Updates. Use type "both" for item/user actions, "admin" for permissions.
 */
public final class ActivityLogger {

    private static final String TYPE_BOTH = "both";
    private static final String TYPE_ADMIN = "admin";

    public static void log(String description, String type) {
        if (description == null || description.isEmpty()) return;
        final String typeToLog = type == null ? TYPE_BOTH : type;
        var user = AuthManager.getInstance().getCurrentUser();
        if (user == null) return;
        final int userId = user.getId();
        final String desc = description;
        new Thread(() -> {
            try {
                APIService.getInstance().logActivity(userId, desc, typeToLog);
            } catch (Exception ignored) { }
        }).start();
    }

    public static void logBoth(String description) {
        log(description, TYPE_BOTH);
    }

    public static void logAdmin(String description) {
        log(description, TYPE_ADMIN);
    }
}
