package com.desktopapp.frontend.managers;

import com.desktopapp.frontend.models.User;

/**
 * Singleton Pattern
 */
public class AuthManager {
    private static AuthManager instance;
    private User currentUser;
    private String token;

    private AuthManager() {
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public void login(String token, User user) {
        this.token = token;
        this.currentUser = user;
    }

    public void logout() {
        this.token = null;
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return token != null && currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getToken() {
        return token;
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
}
