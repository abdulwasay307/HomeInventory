package com.desktopapp.frontend.controllers;

import com.desktopapp.frontend.models.User;
import com.desktopapp.frontend.services.APIService;
import com.desktopapp.frontend.utils.ActivityLogger;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserController {

    public interface View {
        void showLoading(String message);
        void hideLoading();
        void showError(String message);
        void onUsersLoaded(List<User> users);
        void onUserCreated();
        void onUserUpdated();
        void onUserDeleted(int id);
    }

    private final View view;

    public UserController(View view) {
        this.view = view;
    }

    public void loadUsers() {
        view.showLoading("Loading users...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> resultRaw = APIService.getInstance().getAllUsers();
                List<User> userList = new ArrayList<>();
                for (Map<String, Object> map : resultRaw) {
                    int id = getInt(map, "id");
                    String email = getString(map, "email");
                    String role = getString(map, "role");
                    User user = new User(id, email, role);
                    user.setFirstName(getString(map, "first_name"));
                    user.setLastName(getString(map, "last_name"));
                    userList.add(user);
                }
                Platform.runLater(() -> view.onUsersLoaded(userList));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to load users: " + ex.getMessage()));
            }
        }).start();
    }

    public void createUser(Map<String, Object> userData) {
        view.showLoading("Creating user...");
        new Thread(() -> {
            try {
                APIService.getInstance().createUser(userData);
                ActivityLogger.logBoth("User created: " + formatUserDisplayName(getString(userData, "first_name"), getString(userData, "last_name"), getString(userData, "email")));
                Platform.runLater(view::onUserCreated);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to create user: " + ex.getMessage()));
            }
        }).start();
    }

    public void updateUser(Map<String, Object> userData) {
        view.showLoading("Updating user...");
        new Thread(() -> {
            try {
                APIService.getInstance().updateUser(userData);
                ActivityLogger.logBoth("User updated: " + formatUserDisplayName(getString(userData, "first_name"), getString(userData, "last_name"), getString(userData, "email")));
                Platform.runLater(view::onUserUpdated);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to update user: " + ex.getMessage()));
            }
        }).start();
    }

    public void deleteUser(int id, String userDisplayName) {
        view.showLoading("Deleting user...");
        new Thread(() -> {
            try {
                APIService.getInstance().deleteUser(id);
                ActivityLogger.logBoth("User deleted: " + (userDisplayName != null && !userDisplayName.isEmpty() ? userDisplayName : "id " + id));
                Platform.runLater(() -> view.onUserDeleted(id));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to delete user: " + ex.getMessage()));
            }
        }).start();
    }

    private static String formatUserDisplayName(String first, String last, String email) {
        String name = (first != null ? first : "").trim() + " " + (last != null ? last : "").trim();
        name = name.trim();
        return name.isEmpty() ? (email != null ? email : "") : name;
    }

    private int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch(NumberFormatException ignored) {}
        }
        return 0;
    }
    
    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? String.valueOf(v) : "";
    }
}
