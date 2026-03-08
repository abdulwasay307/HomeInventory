package com.desktopapp.frontend.views.items;

import com.desktopapp.frontend.models.Item;
import com.desktopapp.frontend.utils.ViewUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ItemDialog {

    /** Use/consume an item - decrement quantity by amount. */
    public static void showUseDialog(Item item, java.util.function.Consumer<Integer> onUse) {
        int maxQty = Math.max(0, item.getQuantity());
        if (maxQty == 0) return;
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Use Item");
        dialog.setHeaderText("How many of \"" + item.getName() + "\" did you use? (Available: " + maxQty + ")");

        Spinner<Integer> spinner = new Spinner<>(1, maxQty, 1);
        spinner.setEditable(true);
        spinner.setMaxWidth(100);

        VBox content = new VBox(10, new Label("Amount to use:"), spinner);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? spinner.getValue() : null);

        dialog.showAndWait().ifPresent(amount -> {
            if (onUse != null && amount > 0) onUse.accept(amount);
        });
    }
    
    public static void showCreateDialog(Consumer<Map<String, Object>> onSave) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Item");
        dialog.setHeaderText("Add New Item");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setStyle("-fx-background-color: white;");
        
        FormFields fields = createFormFields(null);
        dialogPane.setContent(fields.grid);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return fields.toMap();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (onSave != null) {
                onSave.accept(result);
            }
        });
    }
    
    public static void showEditDialog(Item item, Consumer<Map<String, Object>> onSave) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Update Item Details");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setStyle("-fx-background-color: white;");
        
        FormFields fields = createFormFields(item);
        dialogPane.setContent(fields.grid);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return fields.toMap();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (onSave != null) {
                onSave.accept(result);
            }
        });
    }
    
    private static FormFields createFormFields(Item item) {
        FormFields fields = new FormFields();
        fields.grid.setHgap(10);
        fields.grid.setVgap(10);
        fields.grid.setPadding(new Insets(20));
        
        boolean isEdit = item != null;
        int row = 0;
        
        if (isEdit) {
            fields.idField = new TextField(String.valueOf(item.getId()));
            fields.idField.setDisable(true);
            fields.grid.add(new Label("ID:"), 0, row);
            fields.grid.add(fields.idField, 1, row++);
        }
        
        fields.nameField = new TextField(isEdit ? item.getName() : "");
        fields.descField = new TextArea(isEdit ? item.getDescription() : "");
        fields.descField.setPrefRowCount(2);
        fields.catIdField = new TextField(isEdit ? String.valueOf(item.getCategoryId()) : "0");
        fields.roomIdField = new TextField(isEdit ? String.valueOf(item.getRoomId()) : "0");
        fields.qtyField = new TextField(isEdit ? String.valueOf(item.getQuantity()) : "1");
        fields.conditionField = new TextField(isEdit ? item.getItemCondition() : "");
        fields.brandField = new TextField(isEdit ? item.getItemBrand() : "");
        fields.modelField = new TextField(isEdit ? item.getModel() : "");
        fields.serialField = new TextField(isEdit ? item.getSerialNumber() : "");
        
        fields.purchaseDatePicker = new DatePicker();
        if (isEdit && item.getPurchaseDate() != null) {
            String dateStr = item.getPurchaseDate();
            if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
            dateStr = dateStr.replaceAll("[^0-9-]", "");
            try {
                if (!dateStr.isEmpty() && dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    fields.purchaseDatePicker.setValue(LocalDate.parse(dateStr));
                }
            } catch (Exception e) {
                System.err.println("Failed to parse purchase_date: " + dateStr);
            }
        }
        
        fields.priceField = new TextField(isEdit ? String.valueOf(item.getPurchasePrice()) : "0.0");
        
        fields.warrantyDatePicker = new DatePicker();
        if (isEdit && item.getWarrantyExpiry() != null) {
             String dateStr = item.getWarrantyExpiry();
             if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
             dateStr = dateStr.replaceAll("[^0-9-]", "");
             try {
                if (!dateStr.isEmpty() && dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    fields.warrantyDatePicker.setValue(LocalDate.parse(dateStr));
                }
             } catch (Exception e) {
                 System.err.println("Failed to parse warranty_expiry: " + dateStr);
             }
        }
        
        fields.imageField = new TextField(isEdit ? item.getImageUrl() : "");
        fields.notesField = new TextArea(isEdit ? item.getNotes() : "");
        fields.notesField.setPrefRowCount(2);
        
        fields.grid.add(new Label("Name:"), 0, row++);
        fields.grid.add(fields.nameField, 1, row - 1);
        fields.grid.add(new Label("Description:"), 0, row++);
        fields.grid.add(fields.descField, 1, row - 1);
        fields.grid.add(new Label("Category:"), 0, row++);
        fields.grid.add(fields.catIdField, 1, row - 1);
        fields.grid.add(new Label("Storage (Room ID):"), 0, row++);
        fields.grid.add(fields.roomIdField, 1, row - 1);
        fields.grid.add(new Label("Quantity:"), 0, row++);
        fields.grid.add(fields.qtyField, 1, row - 1);
        fields.grid.add(new Label("Condition:"), 0, row++);
        fields.grid.add(fields.conditionField, 1, row - 1);
        fields.grid.add(new Label("Brand:"), 0, row++);
        fields.grid.add(fields.brandField, 1, row - 1);
        fields.grid.add(new Label("Model:"), 0, row++);
        fields.grid.add(fields.modelField, 1, row - 1);
        fields.grid.add(new Label("Serial:"), 0, row++);
        fields.grid.add(fields.serialField, 1, row - 1);
        fields.grid.add(new Label("Purchase Date:"), 0, row++);
        fields.grid.add(fields.purchaseDatePicker, 1, row - 1);
        fields.grid.add(new Label("Price:"), 0, row++);
        fields.grid.add(fields.priceField, 1, row - 1);
        fields.grid.add(new Label("Expiry/Warranty:"), 0, row++);
        fields.grid.add(fields.warrantyDatePicker, 1, row - 1);
        fields.grid.add(new Label("Image URL:"), 0, row++);
        fields.grid.add(fields.imageField, 1, row - 1);
        fields.grid.add(new Label("Notes:"), 0, row++);
        fields.grid.add(fields.notesField, 1, row - 1);
        
        return fields;
    }
    
    private static class FormFields {
        GridPane grid = new GridPane();
        TextField idField;
        TextField nameField;
        TextArea descField;
        TextField catIdField;
        TextField roomIdField;
        TextField qtyField;
        TextField conditionField;
        TextField brandField;
        TextField modelField;
        TextField serialField;
        DatePicker purchaseDatePicker;
        TextField priceField;
        DatePicker warrantyDatePicker;
        TextField imageField;
        TextArea notesField;
        
        Map<String, Object> toMap() {
            Map<String, Object> data = new HashMap<>();
            if (idField != null) {
                data.put("id", Integer.valueOf(ViewUtils.parseInteger(idField.getText())));
            }
            data.put("name", nameField.getText());
            data.put("description", descField.getText());
            data.put("category_id", Integer.valueOf(ViewUtils.parseInteger(catIdField.getText())));
            data.put("room_id", Integer.valueOf(ViewUtils.parseInteger(roomIdField.getText())));
            data.put("quantity", Integer.valueOf(ViewUtils.parseInteger(qtyField.getText())));
            data.put("item_condition", conditionField.getText());
            data.put("item_brand", brandField.getText());
            data.put("model", modelField.getText());
            data.put("serial_number", serialField.getText());
            LocalDate purchaseDate = purchaseDatePicker.getValue();
            data.put("purchase_date", purchaseDate != null ? purchaseDate.toString() : "");
            data.put("purchase_price", priceField.getText().isEmpty() ? 0.0 : Double.parseDouble(priceField.getText()));
            LocalDate warrantyDate = warrantyDatePicker.getValue();
            data.put("warranty_expiry", warrantyDate != null ? warrantyDate.toString() : "");
            data.put("image_url", imageField.getText());
            data.put("notes", notesField.getText());
            return data;
        }
    }
}
