package com.desktopapp.frontend.controllers;

import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.models.User;
import com.desktopapp.frontend.services.APIService;
import javafx.application.Platform;

import java.util.Map;

public class LoginController {

    public interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void onLoginSuccess();
    }

    private final View view;

    public LoginController(View view) {
        this.view = view;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showError("Please enter both email and password");
            return;
        }

        view.showLoading();

        new Thread(() -> {
            try {
                APIService.LoginResponse response = APIService.getInstance().login(email, password);
                Map<String, Object> userData = response.getUser();
                if (userData == null) {
                    throw new Exception("User data is null");
                }
                
                String role = (String) userData.getOrDefault("role", "user");
                int userId = userData.containsKey("id") ? ((Number) userData.get("id")).intValue() : 0;
                User user = new User(userId, email, role);
                if (userData.containsKey("first_name")) {
                    user.setFirstName((String) userData.get("first_name"));
                }
                if (userData.containsKey("last_name")) {
                    user.setLastName((String) userData.get("last_name"));
                }
                
                AuthManager.getInstance().login(response.getToken(), user);
                
                Platform.runLater(view::onLoginSuccess);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg == null || msg.isEmpty() || msg.contains("Cannot connect")) {
                        msg = "Login failed. Please try again.";
                    }
                    view.hideLoading();
                    view.showError(msg);
                });
            }
        }).start();
    }
}
