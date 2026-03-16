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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.desktopapp.frontend.managers.ThemeManager;

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
    private Runnable onThemeChange;

    public RegisterView(Runnable onRegisterSuccess) {
        this(onRegisterSuccess, null, null);
    }

    public RegisterView(Runnable onRegisterSuccess, Runnable onNavigateToLogin) {
        this(onRegisterSuccess, onNavigateToLogin, null);
    }

    public RegisterView(Runnable onRegisterSuccess, Runnable onNavigateToLogin, Runnable onThemeChange) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateToLogin = onNavigateToLogin;
        this.onThemeChange = onThemeChange;
        this.controller = new RegisterController(this);
        setupUI();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        getStyleClass().add("root-dark");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(400);
        card.getStyleClass().add("card");

        Label title = new Label("Register");
        title.getStyleClass().add("title-page");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.getStyleClass().add("label-error");
        errorLabel.setVisible(false);

        successLabel = new Label();
        successLabel.setWrapText(true);
        successLabel.getStyleClass().add("label-success");
        successLabel.setVisible(false);

        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setPrefWidth(300);
        firstNameField.getStyleClass().add("field-dark");

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setPrefWidth(300);
        lastNameField.getStyleClass().add("field-dark");

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(300);
        emailField.getStyleClass().add("field-dark");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);
        passwordField.getStyleClass().add("field-dark");

        registerButton = new Button("Create Account");
        registerButton.setPrefWidth(300);
        registerButton.setPrefHeight(40);
        registerButton.getStyleClass().add("btn-primary-large");
        registerButton.setOnAction(e -> controller.register(
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            emailField.getText().trim(),
            passwordField.getText()
        ));

        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        loginLink.getStyleClass().add("link-muted");
        loginLink.setOnAction(e -> {
            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });

        card.getChildren().addAll(
            title, errorLabel, successLabel, firstNameField, lastNameField,
            emailField, passwordField, registerButton, loginLink
        );

        if (onThemeChange != null) {
            Button themeToggle = new Button(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
            themeToggle.getStyleClass().add("theme-toggle-btn");
            themeToggle.setTooltip(new javafx.scene.control.Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
            themeToggle.setOnAction(e -> {
                ThemeManager.getInstance().toggle();
                themeToggle.setText(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
                themeToggle.setTooltip(new javafx.scene.control.Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
                if (onThemeChange != null) onThemeChange.run();
            });
            HBox themeRow = new HBox(themeToggle);
            themeRow.setAlignment(Pos.CENTER);
            card.getChildren().add(themeRow);
        }

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
