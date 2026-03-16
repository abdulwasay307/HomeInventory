package com.desktopapp.frontend;

import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.managers.ThemeManager;
import com.desktopapp.frontend.views.DashboardView;
import com.desktopapp.frontend.views.authentication.LoginView;
import com.desktopapp.frontend.views.authentication.RegisterView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main application entry point for the Java Desktop Application
 */
public class MainApplication extends Application {
    private Stage primaryStage;
    private Scene currentScene;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Home Inventory");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        showLogin();
        primaryStage.show();
    }

    private void applyTheme() {
        ThemeManager.getInstance().setCurrentScene(currentScene);
        ThemeManager.getInstance().applyTo(currentScene);
    }

    private void showLogin() {
        ThemeManager.getInstance().setCurrentUser(null);
        StackPane root = new StackPane();
        root.getStyleClass().add("root-dark");
        LoginView loginView = new LoginView(
            () -> Platform.runLater(this::showDashboard),
            () -> Platform.runLater(this::showRegister),
            this::applyTheme
        );
        root.getChildren().add(loginView);
        currentScene = new Scene(root, 1000, 700);
        applyTheme();
        primaryStage.setScene(currentScene);
    }

    private void showRegister() {
        ThemeManager.getInstance().setCurrentUser(null);
        StackPane root = new StackPane();
        root.getStyleClass().add("root-dark");
        RegisterView registerView = new RegisterView(
            () -> Platform.runLater(this::showLogin),
            () -> Platform.runLater(this::showLogin),
            this::applyTheme
        );
        root.getChildren().add(registerView);
        currentScene = new Scene(root, 1000, 700);
        applyTheme();
        primaryStage.setScene(currentScene);
    }

    private void showDashboard() {
        if (AuthManager.getInstance().getCurrentUser() != null) {
            ThemeManager.getInstance().setCurrentUser(AuthManager.getInstance().getCurrentUser().getEmail());
        }
        StackPane root = new StackPane();
        root.getStyleClass().add("root-dark");
        DashboardView dashboardView = new DashboardView(
            () -> Platform.runLater(this::showLogin),
            this::applyTheme
        );
        root.getChildren().add(dashboardView);
        currentScene = new Scene(root, 1000, 700);
        applyTheme();
        primaryStage.setScene(currentScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
