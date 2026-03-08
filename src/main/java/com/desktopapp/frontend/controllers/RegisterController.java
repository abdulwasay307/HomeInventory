package com.desktopapp.frontend.controllers;

import com.desktopapp.frontend.services.APIService;
import javafx.application.Platform;
import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    public interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess(String message);
        void onRegisterSuccess();
    }

    private final View view;

    public RegisterController(View view) {
        this.view = view;
    }

    public void register(String firstName, String lastName, String email, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all fields");
            return;
        }

        view.showLoading();

        new Thread(() -> {
            try {
                Map<String, String> userData = new HashMap<>();
                userData.put("first_name", firstName);
                userData.put("last_name", lastName);
                userData.put("email", email);
                userData.put("password", password);

                APIService.getInstance().register(userData);

                Platform.runLater(() -> {
                    view.showSuccess("Account created successfully. Please sign in.");
                    // Delay before navigating
                    new Thread(() -> {
                        try {
                            Thread.sleep(1200);
                            Platform.runLater(view::onRegisterSuccess);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Registration failed. Please try again.";
                    }
                    view.hideLoading();
                    view.showError(errorMsg);
                });
            }
        }).start();
    }
}
