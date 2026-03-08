package com.desktopapp.frontend.views.authentication;

import com.desktopapp.frontend.controllers.LoginController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox implements LoginController.View {
    private final LoginController controller;
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Runnable onLoginSuccess;
    private Runnable onNavigateToRegister;

    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.controller = new LoginController(this);
        setupUI();
    }

    public LoginView(Runnable onLoginSuccess, Runnable onNavigateToRegister) {
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToRegister = onNavigateToRegister;
        this.controller = new LoginController(this);
        setupUI();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #e0e0e0;");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
        errorLabel.setVisible(false);

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);

        loginButton = new Button("Sign In");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(40);
        loginButton.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        Hyperlink registerLink = new Hyperlink("Register");
        registerLink.setOnAction(e -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            }
        });

        card.getChildren().addAll(title, errorLabel, emailField, passwordField, loginButton, registerLink);
        getChildren().add(card);
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
