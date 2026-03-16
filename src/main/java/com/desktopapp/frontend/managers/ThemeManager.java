package com.desktopapp.frontend.managers;

import javafx.scene.Scene;
import java.net.URL;

import java.util.prefs.Preferences;


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

    public void toggle() {
        dark = !dark;
        prefs().put(PREF_PREFIX + currentUserKey, dark ? THEME_DARK : THEME_LIGHT);
        applyTo(currentScene);
    }

    public void applyTo(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String path = getStylesheetPath();
        URL resource = getClass().getResource(path);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public String getStylesheetPath() {
        return dark ? "/theme-dark.css" : "/theme-light.css";
    }

    /** Full URL for the current theme (for dialogs that have their own scene). */
    public String getCurrentStylesheetUrl() {
        URL r = getClass().getResource(getStylesheetPath());
        return r != null ? r.toExternalForm() : null;
    }
}
