package com.example.integratedworkflowmanager.service;

import com.example.integratedworkflowmanager.entity.*;
import com.example.integratedworkflowmanager.repository.*;
import com.example.integratedworkflowmanager.service.WorkflowService;
import com.example.integratedworkflowmanager.util.ExpressionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowExecutionStepRepository stepRepository;
    private final WorkflowErrorLogRepository errorLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final WorkflowTransactionalService transactionalService;



    @Override
    public Map<String, Object> runWorkflow(String workflowName, Map<String, Object> inputParams) {
        Map<String, Object> context = new HashMap<>(inputParams);
        Map<String, Object> resultMap = new HashMap<>();

        WorkflowExecution execution = WorkflowExecution.builder()
                .workflowName(workflowName)
                .executedAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build();

        execution = transactionalService.saveWorkflowExecution(execution);
        UUID executionId = execution.getExecutionId();

        try {
            WorkflowDefinition def = workflowDefinitionRepository.findByName(workflowName)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowName));

            Map<String, Object> json = objectMapper.readValue(def.getWorkflowJson(), Map.class);
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) json.get("nodes");

            for (Map<String, Object> node : nodes) {
                Integer nodeId = (Integer) node.get("id");
                String nodeName = (String) node.get("name");
                String condition = (String) node.getOrDefault("condition", "true");

                if (!MVEL.evalToBoolean(condition, context)) {
                    log.info("Skipping node {} due to condition", nodeName);
                    continue;
                }

                String url = resolveExpressions((String) node.get("request_url"), context);
                String method = ((String) node.getOrDefault("method", "GET")).toUpperCase();

                Map<String, Object> body = resolveMap((Map<String, Object>) node.get("request_body"), context);
                Map<String, Object> headers = resolveMap((Map<String, Object>) node.get("request_headers"), context);
                Map<String, Object> queryParams = resolveMap((Map<String, Object>) node.get("query_params"), context);

                if (!queryParams.isEmpty()) {
                    url += "?" + buildQuery(queryParams);
                }

                HttpHeaders httpHeaders = new HttpHeaders();
                headers.forEach((k, v) -> httpHeaders.set(k, v.toString()));
                HttpEntity<?> entity = new HttpEntity<>(body.isEmpty() ? null : body, httpHeaders);

                ResponseEntity<String> response;
                try {
                    response = restTemplate.exchange(url, HttpMethod.valueOf(method), entity, String.class);
                } catch (Exception ex) {
                    log.error("Node {} failed: {}", nodeName, ex.getMessage());
                    transactionalService.saveWorkflowStep(execution, nodeId, nodeName, url, body, headers, queryParams, ex.getMessage(), 500);
                    transactionalService.updateWorkflowStatus(executionId, "FAIL");
                    resultMap.put("status", "FAIL");
                    resultMap.put("executionId", executionId);
                    return resultMap;
                }

                String responseBody = response.getBody();
                context.put(nodeName, objectMapper.readValue(responseBody, Object.class));
                transactionalService.saveWorkflowStep(execution, nodeId, nodeName, url, body, headers, queryParams, responseBody, response.getStatusCodeValue());
            }

            transactionalService.updateWorkflowStatus(executionId, "SUCCESS");
            resultMap.put("status", "SUCCESS");
            resultMap.put("executionId", executionId);
            return resultMap;

        } catch (Exception ex) {
            log.error("Workflow execution failed: {}", ex.getMessage());
            transactionalService.saveWorkflowError(executionId, workflowName, ex.getMessage());
            transactionalService.updateWorkflowStatus(executionId, "FAIL");
            resultMap.put("status", "FAIL");
            resultMap.put("executionId", executionId);
            return resultMap;
        }
    }

    private String resolveExpressions(String template, Map<String, Object> contextData) {
        if (template == null) return "";

        if (template.contains("{{")) {
            Map<String, Object> context = createMvelContext(contextData); // <-- fix here
            StringBuffer result = new StringBuffer();
            int start, end = 0;
            while ((start = template.indexOf("{{", end)) != -1) {
                result.append(template, end, start);
                end = template.indexOf("}}", start);
                if (end == -1) break;
                String expression = template.substring(start + 2, end).trim();
                Object evaluated = MVEL.eval(expression, context); // <-- use enhanced context
                result.append(evaluated != null ? evaluated.toString() : "");
                end += 2;
            }
            result.append(template.substring(end));
            return result.toString();
        }

        return template;
    }


    private Map<String, Object> resolveMap(Map<String, Object> raw, Map<String, Object> contextData) {
        Map<String, Object> resolved = new HashMap<>();
        if (raw == null) return resolved;

        Map<String, Object> context = createMvelContext(contextData);

        for (var entry : raw.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String str && str.contains("{{")) {
                String expr = str.replace("{{", "").replace("}}", "");
                Object evaluated = MVEL.eval(expr, context);
                resolved.put(entry.getKey(), evaluated);
            } else {
                resolved.put(entry.getKey(), value);
            }
        }
        return resolved;
    }


    private String buildQuery(Map<String, Object> params) {
        List<String> parts = new ArrayList<>();
        params.forEach((k, v) -> parts.add(k + "=" + v.toString()));
        return String.join("&", parts);
    }

    private Map<String, Object> createMvelContext(Map<String, Object> inputContext) {
        Map<String, Object> context = new HashMap<>(inputContext);
        context.put("base64", ExpressionUtils.class);
        return context;
    }


    // Define a functional interface for MVEL lambdas
    @FunctionalInterface
    interface MVELFunction<T, R> {
        R apply(T t);
    }

    public void saveWorkflowFromJsonFile(String name, String workflowJson) {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .name(name)
                .workflowJson(workflowJson)
                .createdAt(LocalDateTime.now())
                .build();
        workflowDefinitionRepository.save(definition);
    }

}
