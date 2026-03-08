package com.desktopapp.frontend.views.items;

import com.desktopapp.frontend.constants.AppConstants;

import java.time.LocalDate;
import com.desktopapp.frontend.controllers.ItemController;
import com.desktopapp.frontend.models.Item;
import com.desktopapp.frontend.models.ModulePermission;
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

public class ItemsView extends VBox implements ItemController.View {
    private final Label statusLabel;
    private final TableView<Item> table;
    private final ObservableList<Item> items;
    private final ModulePermission permissions;
    private final ItemController controller;
    

    public ItemsView(Label statusLabel) {
        this(statusLabel, new ModulePermission(true, true, true, true));
    }

    public ItemsView(Label statusLabel, ModulePermission permissions) {
        this.statusLabel = statusLabel;
        this.permissions = permissions != null ? permissions : new ModulePermission(true, true, true, true);
        this.controller = new ItemController(this);
        
        setSpacing(AppConstants.SPACING);
        setPadding(new Insets(AppConstants.PADDING));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: " + AppConstants.COLOR_WHITE + ";");

        items = FXCollections.observableArrayList();
        table.setItems(items);

        setupColumns();
        setupActions();

        Button createButton = new Button("Create Item");
        createButton.setStyle(AppConstants.BUTTON_STYLE_PRIMARY);
        createButton.setVisible(this.permissions.isCanCreate());
        createButton.setManaged(this.permissions.isCanCreate());
        createButton.setOnAction(e -> ItemDialog.showCreateDialog(itemData -> controller.createItem(itemData)));

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> controller.loadItems());

        HBox header = new HBox(10, createButton, refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        controller.loadItems();
    }

    private void setupColumns() {
        TableColumn<Item, String> idCol = new TableColumn<>("ID");
        idCol.setMaxWidth(90);
        idCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId()))
        );

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getName())
        );

        TableColumn<Item, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setMaxWidth(90);
        qtyCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getQuantity()))
        );

        TableColumn<Item, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getItemBrand())
        );

        TableColumn<Item, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getModel())
        );

        TableColumn<Item, String> statusCol = new TableColumn<>("Status");
        statusCol.setMaxWidth(140);
        statusCol.setCellValueFactory(data -> {
            Item it = data.getValue();
            String status = computeStatus(it);
            return new SimpleStringProperty(status);
        });

        table.getColumns().addAll(idCol, nameCol, qtyCol, brandCol, modelCol, statusCol);
    }

    private String computeStatus(Item item) {
        StringBuilder sb = new StringBuilder();
        if (item.getQuantity() <= AppConstants.LOW_STOCK_THRESHOLD && item.getQuantity() > 0) {
            sb.append("Low Stock");
        }
        if (item.getQuantity() == 0) {
            sb.append("Out of Stock");
        }
        String expiry = item.getWarrantyExpiry();
        if (expiry != null && !expiry.isEmpty()) {
            try {
                String dateStr = expiry.length() > 10 ? expiry.substring(0, 10) : expiry;
                dateStr = dateStr.replaceAll("[^0-9-]", "");
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate expDate = LocalDate.parse(dateStr);
                    if (expDate.isBefore(LocalDate.now())) {
                        if (sb.length() > 0) sb.append(" | ");
                        sb.append("Expired");
                    } else if (!expDate.isAfter(LocalDate.now().plusDays(AppConstants.EXPIRY_DAYS_WARNING))) {
                        if (sb.length() > 0) sb.append(" | ");
                        sb.append("Expiring Soon");
                    }
                }
            } catch (Exception ignored) {}
        }
        return sb.length() > 0 ? sb.toString() : "OK";
    }

    private void setupActions() {
        if (!permissions.isCanRead() && !permissions.isCanUpdate() && !permissions.isCanDelete()) return;
        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMaxWidth(280);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Item, Void> call(TableColumn<Item, Void> param) {
                return new TableCell<>() {
                    private final Button useBtn = new Button("Use");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox buttonsBox = new HBox(5, useBtn, editBtn, deleteBtn);

                    {
                        useBtn.setVisible(permissions.isCanRead());
                        useBtn.setManaged(permissions.isCanRead());
                        editBtn.setVisible(permissions.isCanUpdate());
                        editBtn.setManaged(permissions.isCanUpdate());
                        deleteBtn.setVisible(permissions.isCanDelete());
                        deleteBtn.setManaged(permissions.isCanDelete());
                        useBtn.setStyle(AppConstants.BUTTON_STYLE_SUCCESS.replace("14px", "12px").replace("8 16", "6 10"));
                        editBtn.setStyle(AppConstants.BUTTON_STYLE_PRIMARY.replace("14px", "12px").replace("8 16", "6 10"));
                        deleteBtn.setStyle("-fx-background-color: #b00020; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 10; -fx-cursor: hand;");

                        useBtn.setOnAction(e -> {
                            Item row = getTableView().getItems().get(getIndex());
                            ItemDialog.showUseDialog(row, amount -> controller.useItem(row, amount));
                        });

                        editBtn.setOnAction(e -> {
                            Item row = getTableView().getItems().get(getIndex());
                            ItemDialog.showEditDialog(row, itemData -> controller.updateItem(itemData));
                        });

                        deleteBtn.setOnAction(e -> {
                            Item row = getTableView().getItems().get(getIndex());
                            controller.deleteItem(row.getId(), row.getName());
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
    public void hideLoading() {
        // Optional
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setVisible(true);
            statusLabel.setText(message); // Could use red color
        });
    }

    @Override
    public void onItemsLoaded(List<Item> loadedItems) {
        Platform.runLater(() -> {
            items.setAll(loadedItems);
            statusLabel.setText("Loaded " + items.size() + " items");
        });
    }

    @Override
    public void onItemDeleted(int id) {
        Platform.runLater(() -> {
             statusLabel.setText("Deleted item " + id);
             controller.loadItems(); // Refresh list
        });
    }

    @Override
    public void onItemCreated() {
        Platform.runLater(() -> {
            statusLabel.setText("Item created successfully");
            controller.loadItems();
        });
    }

    @Override
    public void onItemUpdated() {
        Platform.runLater(() -> {
            statusLabel.setText("Item updated successfully");
            controller.loadItems();
        });
    }
}
