package com.desktopapp.frontend.controllers;

import com.desktopapp.frontend.models.Item;
import com.desktopapp.frontend.services.APIService;
import com.desktopapp.frontend.utils.ActivityLogger;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemController {

    public interface View {
        void showLoading(String message);
        void hideLoading();
        void showError(String message);
        void onItemsLoaded(List<Item> items);
        void onItemDeleted(int id);
        void onItemCreated();
        void onItemUpdated();
    }

    private final View view;

    public ItemController(View view) {
        this.view = view;
    }

    public void loadItems() {
        view.showLoading("Loading items...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> resultRaw = APIService.getInstance().getAllItems();
                List<Item> itemList = new ArrayList<>();
                for (Map<String, Object> map : resultRaw) {
                    Item item = new Item();
                    item.setId(getInt(map, "id"));
                    item.setName(getString(map, "name"));
                    item.setDescription(getString(map, "description"));
                    item.setCategoryId(getInt(map, "category_id"));
                    item.setRoomId(getInt(map, "room_id"));
                    item.setQuantity(getInt(map, "quantity"));
                    item.setItemCondition(getString(map, "item_condition"));
                    item.setItemBrand(getString(map, "item_brand"));
                    item.setModel(getString(map, "model"));
                    item.setSerialNumber(getString(map, "serial_number"));
                    item.setPurchaseDate(getString(map, "purchase_date"));
                    item.setPurchasePrice(getDouble(map, "purchase_price"));
                    item.setWarrantyExpiry(getString(map, "warranty_expiry"));
                    item.setImageUrl(getString(map, "image_url"));
                    item.setNotes(getString(map, "notes"));
                    itemList.add(item);
                }
                Platform.runLater(() -> view.onItemsLoaded(itemList));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to load items: " + ex.getMessage()));
            }
        }).start();
    }

    public void deleteItem(int id, String itemName) {
        view.showLoading("Deleting item...");
        new Thread(() -> {
            try {
                APIService.getInstance().deleteItem(id);
                ActivityLogger.logBoth("Item deleted: " + (itemName != null && !itemName.isEmpty() ? itemName : "id " + id));
                Platform.runLater(() -> view.onItemDeleted(id));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Delete failed: " + ex.getMessage()));
            }
        }).start();
    }

    public void createItem(Map<String, Object> data) {
        view.showLoading("Creating item...");
        new Thread(() -> {
            try {
                APIService.getInstance().createItem(data);
                ActivityLogger.logBoth("Item created: " + data.get("name"));
                Platform.runLater(view::onItemCreated);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Create failed: " + ex.getMessage()));
            }
        }).start();
    }

    public void updateItem(Map<String, Object> data) {
        view.showLoading("Updating item...");
        new Thread(() -> {
            try {
                APIService.getInstance().updateItem(data);
                ActivityLogger.logBoth("Item updated: " + data.get("name"));
                Platform.runLater(view::onItemUpdated);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Update failed: " + ex.getMessage()));
            }
        }).start();
    }

    public void useItem(Item item, int amount) {
        if (amount <= 0 || amount > item.getQuantity()) return;
        Map<String, Object> data = itemToMap(item);
        data.put("quantity", item.getQuantity() - amount);
        view.showLoading("Updating item...");
        new Thread(() -> {
            try {
                APIService.getInstance().updateItem(data);
                ActivityLogger.logBoth("Item used / stock updated: " + item.getName() + " (qty: -" + amount + ")");
                Platform.runLater(view::onItemUpdated);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Update failed: " + ex.getMessage()));
            }
        }).start();
    }

    private Map<String, Object> itemToMap(Item item) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", item.getId());
        m.put("name", item.getName());
        m.put("description", item.getDescription() != null ? item.getDescription() : "");
        m.put("category_id", item.getCategoryId());
        m.put("room_id", item.getRoomId());
        m.put("quantity", item.getQuantity());
        m.put("item_condition", item.getItemCondition() != null ? item.getItemCondition() : "");
        m.put("item_brand", item.getItemBrand() != null ? item.getItemBrand() : "");
        m.put("model", item.getModel() != null ? item.getModel() : "");
        m.put("serial_number", item.getSerialNumber() != null ? item.getSerialNumber() : "");
        m.put("purchase_date", item.getPurchaseDate() != null && !item.getPurchaseDate().isEmpty() ? item.getPurchaseDate() : "1970-01-01");
        m.put("purchase_price", item.getPurchasePrice());
        m.put("warranty_expiry", item.getWarrantyExpiry() != null && !item.getWarrantyExpiry().isEmpty() ? item.getWarrantyExpiry() : "2099-12-31");
        m.put("image_url", item.getImageUrl() != null ? item.getImageUrl() : "");
        m.put("notes", item.getNotes() != null ? item.getNotes() : "");
        return m;
    }

    private int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch(NumberFormatException ignored) {}
        }
        return 0;
    }
    
    private double getDouble(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
             try { return Double.parseDouble((String) v); } catch(NumberFormatException ignored) {}
        }
        return 0.0;
    }
    
    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? String.valueOf(v) : "";
    }
}
