package com.desktopapp.frontend.controllers;

import com.desktopapp.frontend.models.ModulePermission;
import com.desktopapp.frontend.models.User;
import com.desktopapp.frontend.services.APIService;
import com.desktopapp.frontend.utils.ActivityLogger;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionsController {

    public interface View {
        void showLoading(String message);
        void hideLoading();
        void showError(String message);
        void onUsersLoaded(List<User> users);
        void onPermissionsLoaded(List<ModulePermission> permissions);
        void onPermissionsSaved();
        void onUserRoleUpdated(User updatedUser);
    }

    private final View view;

    public PermissionsController(View view) {
        this.view = view;
    }

    public void loadUsers() {
        view.showLoading("Loading users...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> resultRaw = APIService.getInstance().getAllUsers();
                List<User> userList = new ArrayList<>();
                for (Map<String, Object> map : resultRaw) {
                    int id = getInt(map, "id");
                    String email = getString(map, "email");
                    String role = getString(map, "role");
                    User user = new User(id, email, role);
                    user.setFirstName(getString(map, "first_name"));
                    user.setLastName(getString(map, "last_name"));
                    userList.add(user);
                }
                Platform.runLater(() -> view.onUsersLoaded(userList));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to load users: " + ex.getMessage()));
            }
        }).start();
    }

    public void loadUserPermissions(int userId) {
        view.showLoading("Loading permissions...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> perms = APIService.getInstance().getUserPermissions(userId);
                List<ModulePermission> permList = new ArrayList<>();
                
                // We should also fetch all modules to ensure we show all of them, 
                // but for now let's just map what we get. Ideally we join with modules.
                // In the original PermissionsView, it fetched user permissions.
                
                for (Map<String, Object> p : perms) {
                    ModulePermission mp = new ModulePermission();
                    mp.setModuleId(getInt(p, "module_id"));
                    mp.setModuleName(getString(p, "module_name")); // Assuming API returns this
                    mp.setCanRead(getInt(p, "can_read") != 0);
                    mp.setCanCreate(getInt(p, "can_create") != 0);
                    mp.setCanUpdate(getInt(p, "can_update") != 0);
                    mp.setCanDelete(getInt(p, "can_delete") != 0);
                    permList.add(mp);
                }
                Platform.runLater(() -> view.onPermissionsLoaded(permList));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to load permissions: " + ex.getMessage()));
            }
        }).start();
    }

    public void savePermissions(int userId, String userName, List<ModulePermission> permissions) {
        view.showLoading("Saving permissions...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> permList = new ArrayList<>();
                for (ModulePermission p : permissions) {
                     Map<String, Object> map = new java.util.HashMap<>();
                     map.put("module_id", p.getModuleId());
                     map.put("can_read", p.isCanRead() ? 1 : 0);
                     map.put("can_create", p.isCanCreate() ? 1 : 0);
                     map.put("can_update", p.isCanUpdate() ? 1 : 0);
                     map.put("can_delete", p.isCanDelete() ? 1 : 0);
                     permList.add(map);
                }
                APIService.getInstance().updateUserPermissions(userId, permList);
                ActivityLogger.logAdmin("Permissions updated for user: " + (userName != null && !userName.isEmpty() ? userName : "id " + userId));
                Platform.runLater(view::onPermissionsSaved);
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to save permissions: " + ex.getMessage()));
            }
        }).start();
    }

    /** Update a user's role */
    public void setUserRole(User user, String role) {
        if (user == null) return;
        view.showLoading("Updating role...");
        new Thread(() -> {
            try {
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("id", user.getId());
                data.put("first_name", user.getFirstName() != null ? user.getFirstName() : "");
                data.put("last_name", user.getLastName() != null ? user.getLastName() : "");
                data.put("role", role);
                APIService.getInstance().updateUser(data);
                String uName = (user.getFirstName() != null ? user.getFirstName() : "").trim() + " " + (user.getLastName() != null ? user.getLastName() : "").trim();
                uName = uName.trim();
                if (uName.isEmpty()) uName = user.getEmail();
                ActivityLogger.logAdmin(role.equals("admin") ? "User made admin: " + uName : "Admin removed from user: " + uName);
                User updated = new User(user.getId(), user.getEmail(), role);
                updated.setFirstName(user.getFirstName());
                updated.setLastName(user.getLastName());
                Platform.runLater(() -> view.onUserRoleUpdated(updated));
            } catch (Exception ex) {
                Platform.runLater(() -> view.showError("Failed to update role: " + ex.getMessage()));
            }
        }).start();
    }

    private int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch(NumberFormatException ignored) {}
        }
        return 0;
    }
    
    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? String.valueOf(v) : "";
    }
}
