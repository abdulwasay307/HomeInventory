package com.desktopapp.frontend.views;

import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.managers.PermissionsManager;
import com.desktopapp.frontend.views.items.ItemsView;
import com.desktopapp.frontend.views.permissions.PermissionsView;
import com.desktopapp.frontend.views.users.UsersView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.desktopapp.frontend.managers.ThemeManager;

/** Module IDs from DB: 1=Dashboard, 2=Items, 3=Users, 4=Manage Permissions */
public class DashboardView extends VBox {

    private static final int MODULE_DASHBOARD = 1;
    private static final int MODULE_ITEMS = 2;
    private static final int MODULE_USERS = 3;
    private static final int MODULE_PERMISSIONS = 4;

    public DashboardView(Runnable onLogout) {
        this(onLogout, null);
    }

    public DashboardView(Runnable onLogout, Runnable onThemeChange) {
        setSpacing(16);
        setPadding(new Insets(20));
        getStyleClass().add("root-dark");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("title-main");

        String email = AuthManager.getInstance().getCurrentUser() != null
            ? AuthManager.getInstance().getCurrentUser().getEmail()
            : "Guest";
        Label subtitle = new Label("Logged in as: " + email);
        subtitle.getStyleClass().add("label-dim");

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("label-dim");
        statusLabel.setVisible(false);

        Button themeToggle = new Button(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
        themeToggle.getStyleClass().add("theme-toggle-btn");
        themeToggle.setTooltip(new javafx.scene.control.Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
        themeToggle.setOnAction(e -> {
            ThemeManager.getInstance().toggle();
            themeToggle.setText(ThemeManager.getInstance().isDark() ? "\u2600" : "\u263E");
            themeToggle.setTooltip(new javafx.scene.control.Tooltip(ThemeManager.getInstance().isDark() ? "Switch to light" : "Switch to dark"));
            if (onThemeChange != null) onThemeChange.run();
        });

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-primary");
        logoutButton.setOnAction(e -> {
            PermissionsManager.getInstance().clear();
            AuthManager.getInstance().logout();
            if (onLogout != null) onLogout.run();
        });

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, spacer, statusLabel, themeToggle, logoutButton);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane-dark");
        Label loadingLabel = new Label("Loading...");
        loadingLabel.getStyleClass().add("label-dim");
        loadingLabel.setVisible(true);
        StackPane contentPane = new StackPane(tabPane, loadingLabel);
        StackPane.setAlignment(loadingLabel, Pos.CENTER);

        getChildren().addAll(header, contentPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        PermissionsManager.getInstance().loadPermissions(() -> {
            tabPane.getTabs().clear();
            PermissionsManager pm = PermissionsManager.getInstance();

            if (pm.canRead(MODULE_DASHBOARD)) {
                DashboardKpiView dashboardKpiView = new DashboardKpiView();
                Tab dashboardTab = new Tab("Dashboard", dashboardKpiView);
                dashboardTab.setClosable(false);
                tabPane.getTabs().add(dashboardTab);
                tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                    if (newTab == dashboardTab) {
                        dashboardKpiView.refresh();
                        dashboardKpiView.ensureWebSocketConnected();
                    }
                });
            }
            if (pm.canRead(MODULE_USERS)) {
                UsersView usersView = new UsersView(statusLabel, pm.get(MODULE_USERS));
                Tab usersTab = new Tab("Users", usersView);
                usersTab.setClosable(false);
                tabPane.getTabs().add(usersTab);
            }
            if (pm.canRead(MODULE_ITEMS)) {
                ItemsView itemsView = new ItemsView(statusLabel, pm.get(MODULE_ITEMS));
                Tab itemsTab = new Tab("Items", itemsView);
                itemsTab.setClosable(false);
                tabPane.getTabs().add(itemsTab);
            }
            if (pm.canRead(MODULE_PERMISSIONS)) {
                PermissionsView permissionsView = new PermissionsView(statusLabel, pm.get(MODULE_PERMISSIONS));
                Tab permissionsTab = new Tab("Manage Permissions", permissionsView);
                permissionsTab.setClosable(false);
                tabPane.getTabs().add(permissionsTab);
            }

            loadingLabel.setVisible(tabPane.getTabs().isEmpty());
            loadingLabel.setText(tabPane.getTabs().isEmpty()
                ? "No modules accessible. Contact admin for permissions."
                : "Loading...");
        });
    }
}
