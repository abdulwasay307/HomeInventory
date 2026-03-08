package com.desktopapp.frontend.utils;

public class ViewUtils {
    
    public static String formatInteger(Object value) {
        if (value == null || value.toString().isEmpty()) return "";
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        String str = value.toString();
        try {
            double d = Double.parseDouble(str);
            return String.valueOf((int) d);
        } catch (NumberFormatException e) {
            return str;
        }
    }
    
    public static int parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            double d = Double.parseDouble(text.trim());
            return (int) d;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
