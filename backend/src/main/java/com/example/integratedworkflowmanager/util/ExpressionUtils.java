package com.example.integratedworkflowmanager.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ExpressionUtils {
    public static String base64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}

