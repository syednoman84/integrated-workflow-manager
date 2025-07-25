package com.example.integratedworkflowmanager.util;

public class StringUtils {

    // Capitalizes the first letter of a string
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    // Converts to lowercase
    public static String toLower(String input) {
        return input == null ? null : input.toLowerCase();
    }

    // Converts to uppercase
    public static String toUpper(String input) {
        return input == null ? null : input.toUpperCase();
    }

    // Checks if a string contains another string
    public static boolean contains(String source, String target) {
        return source != null && target != null && source.contains(target);
    }

    // Trims whitespace
    public static String trim(String input) {
        return input == null ? null : input.trim();
    }

    // Checks if string is null or empty
    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    // Replaces one string with another
    public static String replace(String input, String target, String replacement) {
        return input == null ? null : input.replace(target, replacement);
    }

    // Extracts a substring from startIndex to endIndex (exclusive)
    public static String substring(String input, int startIndex, int endIndex) {
        if (input == null) return null;
        if (startIndex < 0 || endIndex > input.length() || startIndex >= endIndex) return "";
        return input.substring(startIndex, endIndex);
    }

    // Overload: Extracts a substring from startIndex to the end of the string
    public static String substring(String input, int startIndex) {
        if (input == null) return null;
        if (startIndex < 0 || startIndex > input.length()) return "";
        return input.substring(startIndex);
    }
}

