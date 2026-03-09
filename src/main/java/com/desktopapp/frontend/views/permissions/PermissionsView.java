package com.desktopapp.frontend.views.permissions;

import com.desktopapp.frontend.constants.AppConstants;
import com.desktopapp.frontend.controllers.PermissionsController;
import com.desktopapp.frontend.managers.AuthManager;
import com.desktopapp.frontend.models.ModulePermission;
import com.desktopapp.frontend.models.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PermissionsView extends VBox implements PermissionsController.View {
    private final Label statusLabel;
    private final TableView<ModulePermission> table;
    private final ObservableList<ModulePermission> permissions;
    private final ObservableList<User> users;
    private final ComboBox<User> userCombo;
    private final PermissionsController controller;
    private final Button toggleAdminButton;
    private Integer pendingRoleUpdatedUserId;

    public PermissionsView(Label statusLabel, ModulePermission adminPerms) {
        this.statusLabel = statusLabel;
        this.controller = new PermissionsController(this);
        setSpacing(AppConstants.SPACING);
        setPadding(new Insets(AppConstants.PADDING));
        getStyleClass().add("root-dark");

        Label header = new Label("Manage Permissions");
        header.getStyleClass().add("title-section");

        Label selectUserLabel = new Label("Select User:");
        selectUserLabel.getStyleClass().add("label-dim");

        users = FXCollections.observableArrayList();
        userCombo = new ComboBox<>(users);
        userCombo.setPromptText("Select User");
        userCombo.setPrefWidth(200);
        userCombo.getStyleClass().add("combo-dark");
        userCombo.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getEmail() + " (" + user.getRole() + ")" : "";
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for read-only combos
            }
        });
        
        userCombo.setOnAction(e -> {
            User selected = userCombo.getValue();
            if (selected != null) {
                controller.loadUserPermissions(selected.getId());
            }
        });

        table = new TableView<>();
        table.getStyleClass().add("table-dark");
        permissions = FXCollections.observableArrayList();
        table.setItems(permissions);
        table.setEditable(true); // Important for checkboxes
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        setupColumns();

        toggleAdminButton = new Button();
        toggleAdminButton.getStyleClass().add("btn-primary");
        updateToggleAdminButtonText();
        toggleAdminButton.setOnAction(e -> {
            User selected = userCombo.getValue();
            if (selected == null) {
                showError("Please select a user first.");
                return;
            }
            boolean isAdmin = "admin".equalsIgnoreCase(selected.getRole());
            controller.setUserRole(selected, isAdmin ? "user" : "admin");
        });

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setOnAction(e -> {
             User selected = userCombo.getValue();
             if (selected != null) {
                 String userName = (selected.getFirstName() != null ? selected.getFirstName() : "").trim() + " " + (selected.getLastName() != null ? selected.getLastName() : "").trim();
                 userName = userName.trim();
                 if (userName.isEmpty()) userName = selected.getEmail();
                 controller.savePermissions(selected.getId(), userName, permissions);
             } else {
                 showError("Please select a user first.");
             }
        });

        HBox adminRow = new HBox(10);
        adminRow.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        adminRow.getChildren().addAll(spacer, toggleAdminButton);

        getChildren().addAll(header, selectUserLabel, userCombo, adminRow, table, saveButton);

        controller.loadUsers();
    }

    private void setupColumns() {
        // Module Name
        TableColumn<ModulePermission, String> moduleCol = new TableColumn<>("Module");
        moduleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getModuleName()));

        // Checkbox Columns
        TableColumn<ModulePermission, Boolean> readCol = createCheckboxColumn("Read", ModulePermission::isCanRead, ModulePermission::setCanRead);
        TableColumn<ModulePermission, Boolean> createCol = createCheckboxColumn("Create", ModulePermission::isCanCreate, ModulePermission::setCanCreate);
        TableColumn<ModulePermission, Boolean> updateCol = createCheckboxColumn("Update", ModulePermission::isCanUpdate, ModulePermission::setCanUpdate);
        TableColumn<ModulePermission, Boolean> deleteCol = createCheckboxColumn("Delete", ModulePermission::isCanDelete, ModulePermission::setCanDelete);

        table.getColumns().addAll(moduleCol, readCol, createCol, updateCol, deleteCol);
    }
    
    private TableColumn<ModulePermission, Boolean> createCheckboxColumn(String title, Function<ModulePermission, Boolean> getter, BiConsumer<ModulePermission, Boolean> setter) {
        TableColumn<ModulePermission, Boolean> col = new TableColumn<>(title);
        col.setCellValueFactory(param -> {
            ModulePermission perm = param.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(getter.apply(perm));
            booleanProp.addListener((observable, oldValue, newValue) -> setter.accept(perm, newValue));
            return booleanProp;
        });
        col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
        col.setEditable(true);
        return col;
    }

    private void setAllPermissionsTrue() {
        java.util.List<ModulePermission> updated = new java.util.ArrayList<>();
        for (ModulePermission p : permissions) {
            ModulePermission all = new ModulePermission();
            all.setModuleId(p.getModuleId());
            all.setModuleName(p.getModuleName());
            all.setCanRead(true);
            all.setCanCreate(true);
            all.setCanUpdate(true);
            all.setCanDelete(true);
            updated.add(all);
        }
        permissions.setAll(updated);
    }

    private void updateToggleAdminButtonText() {
        User selected = userCombo.getValue();
        if (selected != null && "admin".equalsIgnoreCase(selected.getRole())) {
            toggleAdminButton.setText("Remove admin");
        } else {
            toggleAdminButton.setText("Make admin");
        }
    }

    // View Interface Implementation

    @Override
    public void showLoading(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @Override
    public void hideLoading() { }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @Override
    public void onUsersLoaded(List<User> userList) {
        Platform.runLater(() -> {
            User currentUser = AuthManager.getInstance().getCurrentUser();
            int currentId = currentUser != null ? currentUser.getId() : -1;
            List<User> others = userList.stream()
                .filter(u -> u.getId() != currentId)
                .collect(Collectors.toList());
            users.setAll(others);
            statusLabel.setText("Users loaded.");
            if (pendingRoleUpdatedUserId != null) {
                User reselect = users.stream()
                    .filter(u -> u.getId() == pendingRoleUpdatedUserId)
                    .findFirst()
                    .orElse(null);
                pendingRoleUpdatedUserId = null;
                if (reselect != null) {
                    userCombo.setValue(reselect);
                    controller.loadUserPermissions(reselect.getId());
                }
            }
        });
    }

    @Override
    public void onPermissionsLoaded(List<ModulePermission> perms) {
        Platform.runLater(() -> {
            permissions.setAll(perms);
            statusLabel.setText("Permissions loaded.");
            User selected = userCombo.getValue();
            boolean isAdmin = selected != null && "admin".equalsIgnoreCase(selected.getRole());
            if (isAdmin) {
                setAllPermissionsTrue();
                table.setEditable(false);
            } else {
                table.setEditable(true);
            }
            updateToggleAdminButtonText();
        });
    }

    @Override
    public void onPermissionsSaved() {
        Platform.runLater(() -> statusLabel.setText("Permissions saved successfully."));
    }

    @Override
    public void onUserRoleUpdated(User updatedUser) {
        Platform.runLater(() -> {
            statusLabel.setText("Role updated successfully.");
            controller.loadUsers();
            pendingRoleUpdatedUserId = updatedUser.getId();
        });
    }
}
