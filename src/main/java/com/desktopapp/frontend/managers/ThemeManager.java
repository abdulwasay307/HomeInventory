package com.desktopapp.frontend.managers;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages dark/light theme: persistence per user and application to the current scene.
 * <p>
 * <b>Where theme preference is stored</b>: Java {@link Preferences} (user node for this package).
 * On macOS: ~/Library/Preferences/com.apple.java.util.prefs (or similar). Keys are
 * "theme_guest" (login screen) and "theme_&lt;email&gt;" for each logged-in user, so each
 * user's choice is remembered and restored when they log in again.
 * </p>
 */
public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String PREF_PREFIX = "theme_";
    private static final String THEME_DARK = "dark";
    private static final String THEME_LIGHT = "light";
    private static final String USER_GUEST = "guest";

    private Scene currentScene;
    private boolean dark = true;
    private String currentUserKey = USER_GUEST;

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(ThemeManager.class);
    }

    private ThemeManager() {
        loadThemeForCurrentUser();
    }

    /**
     * Set the current user so theme is loaded/saved per user. Call with email when user
     * logs in, and with null or "guest" on login screen or after logout. Does not
     * apply to the scene; caller should call applyTo(scene) or applyTheme() after.
     */
    public void setCurrentUser(String userKey) {
        currentUserKey = (userKey == null || userKey.isEmpty()) ? USER_GUEST : userKey;
        loadThemeForCurrentUser();
    }

    private void loadThemeForCurrentUser() {
        String key = PREF_PREFIX + currentUserKey;
        String saved = prefs().get(key, THEME_DARK);
        dark = THEME_DARK.equals(saved);
    }

    public boolean isDark() {
        return dark;
    }

    public boolean isLight() {
        return !dark;
    }

    /** Call when the active scene changes so we can re-apply theme on toggle. */
    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    /** Switch theme, save for current user, and re-apply to current scene. */
    public void toggle() {
        dark = !dark;
        prefs().put(PREF_PREFIX + currentUserKey, dark ? THEME_DARK : THEME_LIGHT);
        applyTo(currentScene);
    }

    /** Apply current theme to the given scene (replace stylesheet). */
    public void applyTo(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String path = getStylesheetPath();
        java.net.URL resource = getClass().getResource(path);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    /** Returns the stylesheet path for the current theme (for initial load). */
    public String getStylesheetPath() {
        return dark ? "/theme-dark.css" : "/theme-light.css";
    }

    /** Full URL for the current theme (for dialogs that have their own scene). */
    public String getCurrentStylesheetUrl() {
        java.net.URL r = getClass().getResource(getStylesheetPath());
        return r != null ? r.toExternalForm() : null;
    }
}
