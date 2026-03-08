package com.desktopapp.frontend.views.authentication;

import com.desktopapp.frontend.controllers.RegisterController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterView extends VBox implements RegisterController.View {
    private final RegisterController controller;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private PasswordField passwordField;
    private Button registerButton;
    private Label errorLabel;
    private Label successLabel;
    private Runnable onRegisterSuccess;
    private Runnable onNavigateToLogin;

    public RegisterView(Runnable onRegisterSuccess) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.controller = new RegisterController(this);
        setupUI();
    }

    public RegisterView(Runnable onRegisterSuccess, Runnable onNavigateToLogin) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateToLogin = onNavigateToLogin;
        this.controller = new RegisterController(this);
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

        Label title = new Label("Register");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
        errorLabel.setVisible(false);

        successLabel = new Label();
        successLabel.setWrapText(true);
        successLabel.setStyle("-fx-text-fill: green; -fx-padding: 10;");
        successLabel.setVisible(false);

        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setPrefWidth(300);

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setPrefWidth(300);

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);

        registerButton = new Button("Create Account");
        registerButton.setPrefWidth(300);
        registerButton.setPrefHeight(40);
        registerButton.setOnAction(e -> controller.register(
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            emailField.getText().trim(),
            passwordField.getText()
        ));

        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        loginLink.setOnAction(e -> {
            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });

        card.getChildren().addAll(
            title, errorLabel, successLabel, firstNameField, lastNameField,
            emailField, passwordField, registerButton, loginLink
        );
        getChildren().add(card);
    }

    @Override
    public void showLoading() {
        Platform.runLater(() -> {
            registerButton.setDisable(true);
            registerButton.setText("Creating...");
            errorLabel.setVisible(false);
            successLabel.setVisible(false);
        });
    }

    @Override
    public void hideLoading() {
        Platform.runLater(() -> {
            registerButton.setDisable(false);
            registerButton.setText("Create Account");
        });
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
        });
    }

    @Override
    public void showSuccess(String message) {
        Platform.runLater(() -> {
            successLabel.setText(message);
            successLabel.setVisible(true);
            errorLabel.setVisible(false);
        });
    }

    @Override
    public void onRegisterSuccess() {
        Platform.runLater(() -> {
             if (onRegisterSuccess != null) {
                 onRegisterSuccess.run();
             }
        });
    }
}
