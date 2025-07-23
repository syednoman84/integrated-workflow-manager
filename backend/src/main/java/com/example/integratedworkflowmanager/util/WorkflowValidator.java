package com.example.integratedworkflowmanager.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class WorkflowValidator {

    public void validate(JsonNode workflowJson) {
        if (!workflowJson.has("nodes") || !workflowJson.get("nodes").isArray()) {
            throw new IllegalArgumentException("Workflow JSON must contain an array field 'nodes'.");
        }

        Set<Integer> ids = new HashSet<>();

        for (JsonNode node : workflowJson.get("nodes")) {
            if (!node.has("id") || !node.has("name") || !node.has("request_url")) {
                throw new IllegalArgumentException("Each node must have 'id', 'name', and 'request_url'.");
            }

            int id = node.get("id").asInt();
            if (!ids.add(id)) {
                throw new IllegalArgumentException("Duplicate node ID found: " + id);
            }

            // Optional: Validate MVEL expressions
            if (node.has("condition")) {
                String condition = node.get("condition").asText();
                if (condition.isBlank()) {
                    throw new IllegalArgumentException("Condition must not be blank.");
                }
                // You can optionally validate MVEL syntax statically
                // But MVEL doesn't have built-in syntax checker without evaluation context
            }
        }
    }
}

