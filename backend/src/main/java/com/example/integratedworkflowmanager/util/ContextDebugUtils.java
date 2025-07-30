package com.example.integratedworkflowmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ContextDebugUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void printContextAsTable(Map<String, Object> context, String title) {
        System.out.println("\n📦 MVEL Context Snapshot: " + title);
        System.out.println("=".repeat(80));
        System.out.printf("%-30s | %-45s%n", "Key", "Value (Shortened)");
        System.out.println("-".repeat(80));

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String strValue;
            try {
                strValue = objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                strValue = String.valueOf(value);
            }

            if (strValue.length() > 40) {
                strValue = strValue.substring(0, 40) + "...";
            }

            System.out.printf("%-30s | %-45s%n", key, strValue);
        }

        System.out.println("=".repeat(80));
    }
}

