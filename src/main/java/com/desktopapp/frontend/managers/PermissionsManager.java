package com.desktopapp.frontend.managers;

import com.desktopapp.frontend.models.ModulePermission;
import com.desktopapp.frontend.services.APIService;
import javafx.application.Platform;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds current user's module permissions from user_module_permissions.
 * Admin role: all permissions granted.
 * User role: permissions loaded from API.
 */
public class PermissionsManager {
    private static PermissionsManager instance;
    private final Map<Integer, ModulePermission> permissionsByModule = new HashMap<>();
    private boolean loaded;
    private Runnable onLoaded;

    private PermissionsManager() {}

    public static PermissionsManager getInstance() {
        if (instance == null) {
            instance = new PermissionsManager();
        }
        return instance;
    }

    public void loadPermissions(Runnable callback) {
        if (AuthManager.getInstance().isAdmin()) {
            loadAdminPermissions(callback);
            return;
        }

        int userId = AuthManager.getInstance().getCurrentUser() != null
            ? AuthManager.getInstance().getCurrentUser().getId()
            : 0;
        if (userId <= 0) {
            loaded = true;
            if (callback != null) callback.run();
            return;
        }

        onLoaded = callback;
        new Thread(() -> {
            try {
                List<Map<String, Object>> perms = APIService.getInstance().getUserPermissions(userId);
                Platform.runLater(() -> {
                    permissionsByModule.clear();
                    for (Map<String, Object> p : perms) {
                        int moduleId = ((Number) p.get("module_id")).intValue();
                        ModulePermission mp = new ModulePermission(
                            getInt(p, "can_read") != 0,
                            getInt(p, "can_create") != 0,
                            getInt(p, "can_update") != 0,
                            getInt(p, "can_delete") != 0
                        );
                        permissionsByModule.put(moduleId, mp);
                    }
                    loaded = true;
                    if (onLoaded != null) onLoaded.run();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loaded = true;
                    if (onLoaded != null) onLoaded.run();
                });
            }
        }).start();
    }

    private void loadAdminPermissions(Runnable callback) {
        new Thread(() -> {
            try {
                // Fetch all modules to dynamically set permissions
                List<Map<String, Object>> modules = APIService.getInstance().getAllModules();
                Platform.runLater(() -> {
                    permissionsByModule.clear();
                    for (Map<String, Object> m : modules) {
                         int id = ((Number)m.get("id")).intValue();
                         permissionsByModule.put(id, new ModulePermission(true, true, true, true));
                    }
                    loaded = true;
                    if (callback != null) callback.run();
                });
            } catch (Exception e) {
                // Fallback if API fails, though for admin ideally we should retry or show error.
                e.printStackTrace();
                Platform.runLater(() -> {
                    setAdminAllLegacy(); // Fallback
                    loaded = true;
                    if (callback != null) callback.run();
                });
            }
        }).start();
    }

    private void setAdminAllLegacy() {
        permissionsByModule.clear();
        for (int i = 1; i <= 4; i++) {
            permissionsByModule.put(i, new ModulePermission(true, true, true, true));
        }
    }

    private static int getInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean canRead(int moduleId) {
        if (AuthManager.getInstance().isAdmin()) return true;
        ModulePermission p = permissionsByModule.get(moduleId);
        return p != null && p.isCanRead();
    }

    public boolean canCreate(int moduleId) {
        if (AuthManager.getInstance().isAdmin()) return true;
        ModulePermission p = permissionsByModule.get(moduleId);
        return p != null && p.isCanCreate();
    }

    public boolean canUpdate(int moduleId) {
        if (AuthManager.getInstance().isAdmin()) return true;
        ModulePermission p = permissionsByModule.get(moduleId);
        return p != null && p.isCanUpdate();
    }

    public boolean canDelete(int moduleId) {
        if (AuthManager.getInstance().isAdmin()) return true;
        ModulePermission p = permissionsByModule.get(moduleId);
        return p != null && p.isCanDelete();
    }

    public ModulePermission get(int moduleId) {
        if (AuthManager.getInstance().isAdmin()) {
            return new ModulePermission(true, true, true, true);
        }
        return permissionsByModule.getOrDefault(moduleId, new ModulePermission(false, false, false, false));
    }

    public void clear() {
        permissionsByModule.clear();
        loaded = false;
    }
}
