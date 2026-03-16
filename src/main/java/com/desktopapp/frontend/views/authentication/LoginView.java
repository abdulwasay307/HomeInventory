package com.desktopapp.frontend.views.authentication;

import com.desktopapp.frontend.controllers.LoginController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.desktopapp.frontend.managers.ThemeManager;

public class LoginView extends StackPane implements LoginController.View {
    private final LoginController controller;
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Runnable onLoginSuccess;
    private Runnable onNavigateToRegister;
    private Runnable onThemeChange;

    public LoginView(Runnable onLoginSuccess) {
        this(onLoginSuccess, null, null);
    }

    public LoginView(Runnable onLoginSuccess, Runnable onNavigateToRegister) {
        this(onLoginSuccess, onNavigateToRegister, null);
    }

    public LoginView(Runnable onLoginSuccess, Runnable onNavigateToRegister, Runnable onThemeChange) {
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToRegister = onNavigateToRegister;
        this.onThemeChange = onThemeChange;
        this.controller = new LoginController(this);
        setupUI();
    }

    private void setupUI() {
        getStyleClass().add("root-dark");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(400);
        card.getStyleClass().add("card");

        Label title = new Label("Login");
        title.getStyleClass().add("title-page");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.getStyleClass().add("label-error");
        errorLabel.setVisible(false);

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(300);
        emailField.getStyleClass().add("field-dark");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);
        passwordField.getStyleClass().add("field-dark");

        loginButton = new Button("Sign In");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(40);
        loginButton.getStyleClass().add("btn-primary-large");
        loginButton.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        Hyperlink registerLink = new Hyperlink("Register");
        registerLink.getStyleClass().add("link-muted");
        registerLink.setOnAction(e -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            }
        });

        card.getChildren().addAll(title, errorLabel, emailField, passwordField, loginButton, registerLink);
        getChildren().add(card);

        if (onThemeChange != null) {
            Button themeToggle = new Button(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
            themeToggle.getStyleClass().add("theme-toggle-btn");
            themeToggle.setTooltip(new Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
            themeToggle.setOnAction(e -> {
                ThemeManager.getInstance().toggle();
                themeToggle.setText(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
                themeToggle.setTooltip(new Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
                if (onThemeChange != null) onThemeChange.run();
            });
            getChildren().add(themeToggle);
            StackPane.setAlignment(themeToggle, Pos.TOP_RIGHT);
            StackPane.setMargin(themeToggle, new Insets(12, 16, 0, 0));
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        controller.login(email, password);
    }

    @Override
    public void showLoading() {
        Platform.runLater(() -> {
            loginButton.setDisable(true);
            loginButton.setText("Signing in...");
            errorLabel.setVisible(false);
        });
    }

    @Override
    public void hideLoading() {
        Platform.runLater(() -> {
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
        });
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        });
    }

    @Override
    public void onLoginSuccess() {
        Platform.runLater(() -> {
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        });
    }
}
