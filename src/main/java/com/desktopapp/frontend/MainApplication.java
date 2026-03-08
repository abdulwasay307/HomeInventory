package com.desktopapp.frontend;

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

    private void showLogin() {
        StackPane root = new StackPane();
        LoginView loginView = new LoginView(
            () -> Platform.runLater(this::showDashboard),
            () -> Platform.runLater(this::showRegister)
        );
        root.getChildren().add(loginView);
        currentScene = new Scene(root, 1000, 700);
        primaryStage.setScene(currentScene);
    }

    private void showRegister() {
        StackPane root = new StackPane();
        RegisterView registerView = new RegisterView(
            () -> Platform.runLater(this::showLogin),
            () -> Platform.runLater(this::showLogin)
        );
        root.getChildren().add(registerView);
        currentScene = new Scene(root, 1000, 700);
        primaryStage.setScene(currentScene);
    }

    private void showDashboard() {
        StackPane root = new StackPane();
        DashboardView dashboardView = new DashboardView(() -> Platform.runLater(this::showLogin));
        root.getChildren().add(dashboardView);
        currentScene = new Scene(root, 1000, 700);
        primaryStage.setScene(currentScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
