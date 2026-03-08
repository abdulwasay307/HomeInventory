package com.desktopapp.backend.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; 
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder()
        .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static Map<String, Object> fromJsonToMap(String json) {
        Map<String, Object> result = gson.fromJson(json, Map.class);
        return result;
    }

    public static Map<String, Object> fromJsonToMap(InputStream inputStream) {
        try {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return fromJsonToMap(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}   