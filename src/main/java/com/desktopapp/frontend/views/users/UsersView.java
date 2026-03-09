package com.desktopapp.frontend.views.users;

import com.desktopapp.frontend.constants.AppConstants;
import com.desktopapp.frontend.controllers.UserController;
import com.desktopapp.frontend.models.ModulePermission;
import com.desktopapp.frontend.models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;
import java.util.List;

public class UsersView extends VBox implements UserController.View {
    private final Label statusLabel;
    private final TableView<User> table;
    private final ObservableList<User> users;
    private final ModulePermission permissions;
    private final UserController controller;

    public UsersView(Label statusLabel) {
        this(statusLabel, new ModulePermission(true, true, true, true));
    }

    public UsersView(Label statusLabel, ModulePermission permissions) {
        this.statusLabel = statusLabel;
        this.permissions = permissions != null ? permissions : new ModulePermission(true, true, true, true);
        this.controller = new UserController(this);
        setSpacing(AppConstants.SPACING);
        setPadding(new Insets(AppConstants.PADDING));
        getStyleClass().add("root-dark");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getStyleClass().add("table-dark");

        users = FXCollections.observableArrayList();
        table.setItems(users);

        setupColumns();
        setupActions();

        Button createButton = new Button("Create User");
        createButton.getStyleClass().add("btn-primary");
        createButton.setVisible(this.permissions.isCanCreate());
        createButton.setManaged(this.permissions.isCanCreate());
        createButton.setOnAction(e -> UserDialog.showCreate(statusLabel, data -> controller.createUser(data)));

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("btn-secondary");
        refreshButton.setOnAction(e -> controller.loadUsers());

        HBox header = new HBox(10, createButton, refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        controller.loadUsers();
    }

    private void setupColumns() {
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setMaxWidth(60);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        TableColumn<User, String> nameCol = new TableColumn<>("First Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
        
        TableColumn<User, String> surnameCol = new TableColumn<>("Last Name");
        surnameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));

        table.getColumns().addAll(idCol, emailCol, roleCol, nameCol, surnameCol);
    }

    private void setupActions() {
        if (!permissions.isCanUpdate() && !permissions.isCanDelete()) return;
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMaxWidth(200);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox buttonsBox = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.setVisible(permissions.isCanUpdate());
                        editBtn.setManaged(permissions.isCanUpdate());
                        deleteBtn.setVisible(permissions.isCanDelete());
                        deleteBtn.setManaged(permissions.isCanDelete());
                        editBtn.getStyleClass().add("btn-primary-small");
                        deleteBtn.getStyleClass().add("btn-danger");

                        editBtn.setOnAction(e -> {
                            User user = getTableView().getItems().get(getIndex());
                            UserDialog.showEdit(user, statusLabel, data -> controller.updateUser(data));
                        });

                        deleteBtn.setOnAction(e -> {
                            User user = getTableView().getItems().get(getIndex());
                            String displayName = (user.getFirstName() != null ? user.getFirstName() : "").trim() + " " + (user.getLastName() != null ? user.getLastName() : "").trim();
                            displayName = displayName.trim();
                            if (displayName.isEmpty()) displayName = user.getEmail();
                            controller.deleteUser(user.getId(), displayName);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        });

        table.getColumns().add(actionsCol);
    }
    
    // View Interface Implementation

    @Override
    public void showLoading(String message) {
        Platform.runLater(() -> {
            statusLabel.setVisible(true);
            statusLabel.setText(message);
        });
    }

    @Override
    public void hideLoading() { }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    @Override
    public void onUsersLoaded(List<User> userList) {
        Platform.runLater(() -> {
            users.setAll(userList);
            statusLabel.setText("Loaded " + users.size() + " users");
        });
    }

    @Override
    public void onUserCreated() {
         Platform.runLater(() -> {
            statusLabel.setText("User created successfully");
            controller.loadUsers();
        });
    }

    @Override
    public void onUserUpdated() {
        Platform.runLater(() -> {
            statusLabel.setText("User updated successfully");
            controller.loadUsers();
        });
    }

    @Override
    public void onUserDeleted(int id) {
        Platform.runLater(() -> {
            statusLabel.setText("Deleted user " + id);
            controller.loadUsers();
        });
    }
}
