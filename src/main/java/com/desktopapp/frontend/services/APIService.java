package com.desktopapp.frontend.services;

import com.desktopapp.frontend.managers.AuthManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class APIService {
    private static APIService instance;
    private static final String API_BASE_URL = "http://localhost:5000/api";
    private static final Gson gson = new Gson();

    private APIService() {}

    public static APIService getInstance() {
        if (instance == null) {
            instance = new APIService();
        }
        return instance;
    }

    /**
     * Make HTTP request helper method
     */
    private String makeRequest(String method, String endpoint, String requestBody, boolean requiresAuth) throws Exception {
        URL url = new URL(API_BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            
            if (requiresAuth) {
               // Will implement later
            }
            
            if (requestBody != null && !requestBody.isEmpty()) {
                connection.setDoOutput(true); // should be enabled for POST, PUT, DELETE requests , nby default HttpURLConnection is for get requests only
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            int responseCode;
            try {
                responseCode = connection.getResponseCode();
                System.out.println("Response code: " + responseCode);
            } catch (java.net.SocketTimeoutException e) {
                throw new Exception("Connection timeout: Backend server may not be running or is not responding");
            } catch (java.net.ConnectException e) {
                throw new Exception("Cannot connect to backend server. Make sure the server is running on " + API_BASE_URL);
            } catch (Exception e) {
                throw new Exception("Connection error: " + e.getMessage());
            }
            
            String response = "";
            try {
                InputStream inputStream = responseCode >= 200 && responseCode < 300 
                    ? connection.getInputStream() 
                    : connection.getErrorStream();
                
                if (inputStream != null) {
                    //Scanner is used to read the response from the server ( convertsd inputstream to string)
                    try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                        scanner.useDelimiter("\\A"); // reads entire response at once
                        response = scanner.hasNext() ? scanner.next() : "";
                        System.out.println("Response body length: " + response.length());
                        if (response.length() > 0 && response.length() < 500) {
                            System.out.println("Response body: " + response);
                        }
                    }
                } else {
                    System.err.println("Input stream is null for response code: " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("Error reading response: " + e.getMessage());
                e.printStackTrace();
                throw new Exception("Error reading response: " + e.getMessage());
            }
            
            if (responseCode >= 200 && responseCode < 300) {
                if (response == null || response.trim().isEmpty()) {
                    throw new Exception("Empty response from server (HTTP " + responseCode + ")");
                }
                return response;
            } else {
                try {
                    if (response != null && !response.isEmpty()) {
                        Map<String, Object> error = gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
                        String message = (String) error.get("message");
                        throw new Exception(message != null ? message : "HTTP " + responseCode + " error");
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("HTTP")) {
                        throw e; 
                    }
                }
                throw new Exception("HTTP " + responseCode + " error: " + response);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Login with email and password
     */
    public LoginResponse login(String email, String password) throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);
        
        String requestBody = gson.toJson(loginData);
        System.out.println("Making login request to: " + API_BASE_URL + "/auth/login");
        String response = makeRequest("POST", "/auth/login", requestBody, false);
        
        System.out.println("Raw response: " + response);
        
        if (response == null || response.trim().isEmpty()) {
            throw new Exception("Empty response from server");
        }
        
        Map<String, Object> responseMap = gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
        
        if (responseMap == null) {
            throw new Exception("Failed to parse login response");
        }
        
        System.out.println("Parsed response map: " + responseMap);
        
        String token = (String) responseMap.get("token");
        if (token == null || token.isEmpty()) {
            throw new Exception("No token received from server");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) responseMap.get("user");
        
        if (user == null) {
            throw new Exception("No user data received from server");
        }
        
        System.out.println("LoginResponse created successfully");
        return new LoginResponse(token, user);
    }

    /**
     * Register a new user
     */
    public void register(Map<String, String> userData) throws Exception {
        String requestBody = gson.toJson(userData);
        System.out.println("Making register request to: " + API_BASE_URL + "/auth/register");
        makeRequest("POST", "/auth/register", requestBody, false);
    }

    /**
     * Fetch all items.
     * Backend route: GET /api/items/
     */
    public List<Map<String, Object>> getAllItems() throws Exception {
        String response = makeRequest("GET", "/items/", null, true);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    /**
     * Create a new item.
     * Backend route: POST /api/items/add
     */
    public void createItem(Map<String, Object> itemData) throws Exception {
        String requestBody = gson.toJson(itemData);
        makeRequest("POST", "/items/add", requestBody, true);
    }

    /**
     * Update an item.
     * Backend route: PUT /api/items/edit
     */
    public void updateItem(Map<String, Object> itemData) throws Exception {
        String requestBody = gson.toJson(itemData);
        makeRequest("PUT", "/items/edit", requestBody, true);
    }

    /**
     * Delete an item by id.
     * Backend route: DELETE /api/items/remove  (body: {"id": 123})
     */
    public void deleteItem(int id) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        String requestBody = gson.toJson(body);
        makeRequest("DELETE", "/items/remove", requestBody, true);
    }

    /**
     * Fetch all users.
     * Backend route: GET /api/users/
     */
    public List<Map<String, Object>> getAllUsers() throws Exception {
        String response = makeRequest("GET", "/users/", null, true);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    /**
     * Create a new user.
     * Backend route: POST /api/users/add
     */
    public void createUser(Map<String, Object> userData) throws Exception {
        String requestBody = gson.toJson(userData);
        makeRequest("POST", "/users/add", requestBody, true);
    }

    /**
     * Update a user.
     * Backend route: PUT /api/users/edit
     */
    public void updateUser(Map<String, Object> userData) throws Exception {
        String requestBody = gson.toJson(userData);
        makeRequest("PUT", "/users/edit", requestBody, true);
    }

    /**
     * Delete a user by id.
     * Backend route: DELETE /api/users/remove  (body: {"id": 123})
     */
    public void deleteUser(int id) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        String requestBody = gson.toJson(body);
        makeRequest("DELETE", "/users/remove", requestBody, true);
    }

    /**
     * Fetch all modules.
     * Backend route: GET /api/permissions/modules
     */
    public List<Map<String, Object>> getAllModules() throws Exception {
        String response = makeRequest("GET", "/permissions/modules", null, true);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    /**
     * Get permissions for a user.
     * Backend route: GET /api/permissions/user?user_id=X
     */
    public List<Map<String, Object>> getUserPermissions(int userId) throws Exception {
        String response = makeRequest("GET", "/permissions/user?user_id=" + userId, null, true);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    /**
     * Update user permissions.
     * Backend route: PUT /api/permissions/user
     */
    public void updateUserPermissions(int userId, List<Map<String, Object>> permissions) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("permissions", permissions);
        String requestBody = gson.toJson(body);
        makeRequest("PUT", "/permissions/user", requestBody, true);
    }

    /**
     * Log an activity for dashboard Recent Updates.
     * Backend route: POST /api/activity/add  (body: user_id, description, type)
     */
    public void logActivity(int userId, String description, String type) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("description", description);
        body.put("type", type);
        String requestBody = gson.toJson(body);
        makeRequest("POST", "/activity/add", requestBody, true);
    }

    /**
     * Get recent activity for dashboard.
     * Backend route: GET /api/activity/?for_role=admin|user
     */
    public List<Map<String, Object>> getRecentActivity(String forRole) throws Exception {
        String endpoint = "/activity/?for_role=" + (forRole != null ? forRole : "both");
        String response = makeRequest("GET", endpoint, null, true);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    /**
     * Get authorization header value
     */
    private String getAuthHeader() {
        String token = AuthManager.getInstance().getToken();
        return token != null ? "Bearer " + token : null;
    }

    // Response classes
    public static class LoginResponse {
        private String token;
        private Map<String, Object> user;

        public LoginResponse(String token, Map<String, Object> user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public Map<String, Object> getUser() {
            return user;
        }
    }
}