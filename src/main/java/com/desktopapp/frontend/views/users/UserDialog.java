package com.desktopapp.frontend.views.users;

import com.desktopapp.frontend.models.User;
import com.desktopapp.frontend.utils.ViewUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserDialog {
    
    public static void showCreate(Label statusLabel, Consumer<Map<String, Object>> onSave) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create User");
        dialog.setHeaderText("Add New User");
        
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
    
    public static void showEdit(User user, Label statusLabel, Consumer<Map<String, Object>> onSave) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Update User Details");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setStyle("-fx-background-color: white;");
        
        FormFields fields = createFormFields(user);
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
    
    private static FormFields createFormFields(User user) {
        FormFields fields = new FormFields();
        fields.grid.setHgap(10);
        fields.grid.setVgap(10);
        fields.grid.setPadding(new Insets(20));
        
        boolean isEdit = user != null;
        int row = 0;
        
        if (isEdit) {
            fields.idField = new TextField(String.valueOf(user.getId()));
            fields.idField.setDisable(true);
            fields.grid.add(new Label("ID:"), 0, row);
            fields.grid.add(fields.idField, 1, row++);
        }
        
        fields.firstNameField = new TextField(isEdit ? user.getFirstName() : "");
        fields.lastNameField = new TextField(isEdit ? user.getLastName() : "");
        fields.emailField = new TextField(isEdit ? user.getEmail() : "");
        
        fields.roleBox = new ComboBox<>();
        fields.roleBox.getItems().addAll("admin", "user", "manager");
        fields.roleBox.setValue(isEdit ? user.getRole() : "user");
        
        fields.passwordField = new PasswordField(); 
        
        fields.grid.add(new Label("First Name:"), 0, row++);
        fields.grid.add(fields.firstNameField, 1, row - 1);
        fields.grid.add(new Label("Last Name:"), 0, row++);
        fields.grid.add(fields.lastNameField, 1, row - 1);
        fields.grid.add(new Label("Email:"), 0, row++);
        fields.grid.add(fields.emailField, 1, row - 1);
        fields.grid.add(new Label("Role:"), 0, row++);
        fields.grid.add(fields.roleBox, 1, row - 1);
        
        if (!isEdit) {
            fields.grid.add(new Label("Password:"), 0, row++);
            fields.grid.add(fields.passwordField, 1, row - 1);
        }
        
        return fields;
    }
    
    private static class FormFields {
        GridPane grid = new GridPane();
        TextField idField;
        TextField firstNameField;
        TextField lastNameField;
        TextField emailField;
        ComboBox<String> roleBox;
        PasswordField passwordField;
        
        Map<String, Object> toMap() {
            Map<String, Object> data = new HashMap<>();
            if (idField != null) {
                data.put("id", Integer.valueOf(ViewUtils.parseInteger(idField.getText())));
            }
            data.put("first_name", firstNameField.getText());
            data.put("last_name", lastNameField.getText());
            data.put("email", emailField.getText());
            data.put("role", roleBox.getValue());
            if (passwordField != null && !passwordField.getText().isEmpty()) {
                data.put("password", passwordField.getText());
            }
            return data;
        }
    }
}
