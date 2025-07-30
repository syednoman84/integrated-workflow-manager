package com.example.integratedworkflowmanager.service;

import com.example.integratedworkflowmanager.dto.ExecutionStepDto;
import com.example.integratedworkflowmanager.dto.WorkflowExecutionDto;
import com.example.integratedworkflowmanager.entity.*;
import com.example.integratedworkflowmanager.interfaces.FunctionRegistry;
import com.example.integratedworkflowmanager.interfaces.MVELBiFunction;
import com.example.integratedworkflowmanager.interfaces.MVELFunction;
import com.example.integratedworkflowmanager.repository.*;
import com.example.integratedworkflowmanager.service.WorkflowService;
import com.example.integratedworkflowmanager.util.ContextDebugUtils;
import com.example.integratedworkflowmanager.util.ExpressionUtils;
import com.example.integratedworkflowmanager.util.MathUtils;
import com.example.integratedworkflowmanager.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    private final WorkflowExecutionStepRepository workflowExecutionStepRepository;

    /*
        High-level flow of this method:
            1. Loads the workflow by name
            2. Loops through each node (step)
            3. Evaluates its condition (optional MVEL logic)
            4. Skips if the condition is false or already run (idempotency)
            5. Otherwise builds and sends the API call
            6. Saves each step’s execution info
            7. Supports retries for failure
            8. Returns final result status

            ****** Flowchart TD ******
                A[runWorkflow()] --> B[Loop over nodes]
                B --> C{Condition met?}
                C -- No --> D[Skip Node]
                C -- Yes --> E{Idempotent?}
                E -- Already run --> F[Log step as skipped]
                E -- Not run --> G[Resolve Expressions]
                G --> H[Build Request (URL, Body, Headers, Params)]
                H --> I[Send API call with retries]
                I --> J[Log Step]
                J --> B
                B --> K[Update Workflow Status]
                K --> L[Return result map]
     */
    @Override
    public Map<String, Object> runWorkflow(String workflowName, Map<String, Object> inputParams) {
        Map<String, Object> context = new HashMap<>(inputParams);
        Map<String, Object> resultMap = new HashMap<>();
        String applicationId = (String) inputParams.get("applicationId"); // assume it's passed in payload

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

                Map<String, Object> mvelContext = createMvelContext(context);

                // Printing Context Map on console
                ContextDebugUtils.printContextAsTable(mvelContext, "Before Evaluating Condition for Node: " + nodeName);

                log.debug("Evaluating condition: {}", condition);
                log.debug("Available keys: {}", mvelContext.keySet());

                if (!MVEL.evalToBoolean(condition, mvelContext)){
                    log.info("Skipping node {} due to condition", nodeName);
                    continue;
                }

                // 💡 Check idempotency
                String rawKey = (String) node.get("idempotency_key");
                String idempotencyKey = resolveExpressions(rawKey, context); // resolve {{applicationId}} or other dynamic parts

                if (rawKey != null) {
                    if (applicationId != null && idempotencyKey != null) {
                        boolean alreadyExecuted = stepRepository
                                .existsByApplicationIdAndExecution_WorkflowNameAndNodeNameAndSkippedFalse(
                                        applicationId,
                                        workflowName,
                                        nodeName
                                );

                        if (alreadyExecuted) {
                            log.info("⏭️ Skipping node {} due to idempotencyKey match (key: {})", nodeName, idempotencyKey);

                            transactionalService.saveWorkflowStep(
                                    WorkflowExecutionStep.builder()
                                            .execution(execution)
                                            .nodeId(nodeId)
                                            .nodeName(nodeName)
                                            .applicationId(applicationId)
                                            .idempotencyKey(idempotencyKey)
                                            .skipped(true)
                                            .statusCode(0)
                                            .build()
                            );

                            continue;
                        }
                    }
                } else {
                    log.debug("ℹ️ Node {} does not define idempotency_key — skipping idempotency check", nodeName);
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

                Integer maxRetries = (Integer) node.getOrDefault("retry", 0);
                int attempt = 0;
                boolean success = false;

                while (attempt <= maxRetries && !success) {
                    attempt++;

                    try {
                        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.valueOf(method), entity, String.class);
                        String responseBody = response.getBody();
                        context.put(nodeName, objectMapper.readValue(responseBody, Object.class));

                        transactionalService.saveWorkflowStep(
                                WorkflowExecutionStep.builder()
                                        .execution(execution)
                                        .nodeId(nodeId)
                                        .nodeName(nodeName)
                                        .requestUrl(url)
                                        .requestBody(objectMapper.writeValueAsString(body))
                                        .requestHeaders(objectMapper.writeValueAsString(headers))
                                        .queryParams(objectMapper.writeValueAsString(queryParams))
                                        .response(responseBody)
                                        .statusCode(response.getStatusCodeValue())
                                        .applicationId(applicationId)
                                        .idempotencyKey(idempotencyKey)
                                        .skipped(false)
                                        .attemptCount(attempt)
                                        .build()
                        );

                        success = true;
                    } catch (Exception ex) {
                        log.warn("Attempt {} failed for node {}: {}", attempt, nodeName, ex.getMessage());

                        transactionalService.saveWorkflowStep(
                                WorkflowExecutionStep.builder()
                                        .execution(execution)
                                        .nodeId(nodeId)
                                        .nodeName(nodeName)
                                        .requestUrl(url)
                                        .requestBody(objectMapper.writeValueAsString(body))
                                        .requestHeaders(objectMapper.writeValueAsString(headers))
                                        .queryParams(objectMapper.writeValueAsString(queryParams))
                                        .response(ex.getMessage())
                                        .statusCode(500)
                                        .applicationId(applicationId)
                                        .idempotencyKey(idempotencyKey)
                                        .skipped(false)
                                        .attemptCount(attempt)
                                        .build()
                        );

                        if (attempt > maxRetries) {
                            transactionalService.updateWorkflowStatus(executionId, "FAIL");
                            resultMap.put("status", "FAIL");
                            resultMap.put("executionId", executionId);
                            return resultMap;
                        }
                }
}

            } // for loop ends

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
            resultMap.put("error", ex.getMessage());
            return resultMap;
        }
    }

    /*
        This method replaces placeholders like {{applicationId}} in single values:

            1. https://api.example.com/data/{{applicationId}} with actual values from the context map.
            2. Looks for {{...}} patterns
            3. Evaluates the expression using MVEL with the enhanced context
            4. Returns a string with expressions resolved
            5. Used for request_url, idempotency_key, etc.
    */
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

    /*
        This takes a json object (key-value pairs) as map for headers, body, queryParams that may contain dynamic strings like:

            {
              "Authorization": "Bearer {{token}}",
              "applicantId": "{{applicationId}}"
            }

            - and evaluates all string values that contain {{...}} using MVEL.
            - Used for resolving maps like headers/body/params before sending requests.
    */
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

    /*
        Used when appending query params to request_url.
        Converts a map like:
            {
              "userId": "123",
              "active": true
            }

        Into a query string: userId=123&active=true
    */
    private String buildQuery(Map<String, Object> params) {
        List<String> parts = new ArrayList<>();
        params.forEach((k, v) -> parts.add(k + "=" + v.toString()));
        return String.join("&", parts);
    }

    /*
        Creates an MVEL-friendly context with:
            1. All input variables (applicationId, etc.)
            2. Utility functions like:
                - math → MathUtils
                - stringUtils → StringUtils
                - base64 → ExpressionUtils (your custom)
            3. This allows you to use helper functions like:
                - {{math.min(income, 5000)}}
                - {{stringUtils.trim(name)}}
            4. In below example, we are showing the usage of custom functions from 3 different ways:
                - Util class like MathUtils and StringUtils
                - Inline direct call to functional interfaces like MVELFunction<T, R> and MVELBiFunction<T, U, R>
                - Calling methods from a custom registry class like FunctionRegistry which has implemenation of all functions
            5. Note that when you are using all these 3 methods, you have to be careful about the names of the functions to avoid collisions.
    */
    private Map<String, Object> createMvelContext(Map<String, Object> inputContext) {
        // Below line adds all the methods from FunctionRegistry class to the mvel context
        Map<String, Object> context = FunctionRegistry.getMvelContext(inputContext);

        // Printing context keys and values
        context.keySet().stream().sorted()
                .forEach(k -> System.out.println(k + " → " + context.get(k)));

        return context;

        /*
        You can have separate classes having certain methods defined in them
        and add them to the mvel context like below:

            context.put("base64", ExpressionUtils.class);
            context.put("math", MathUtils.class);
            context.put("stringUtils", StringUtils.class);
        */

        /*
        You can have inline lambda implementations defined for the
        Functional Interfaces and add them to the context like below:

            // Lambda-based direct functional interface call using MVELFunction<T, R>
            context.put("toUpper", (MVELFunction<String, String>) String::toUpperCase);
            context.put("trim", (MVELFunction<String, String>) String::trim);
            context.put("square", (MVELFunction<Integer, Integer>) x -> x * x);
            context.put("doubleIt", (MVELFunction<Number, Number>) x -> x.doubleValue() * 2);
            context.put("isEven", (MVELFunction<Integer, Boolean>) x -> x % 2 == 0);

            // Lambda-based direct functional interface call using MVELBiFunction<T, U, R>
            context.put("add", (MVELBiFunction<Number, Number, Number>) (a, b) -> a.doubleValue() + b.doubleValue());
            context.put("subtract", (MVELBiFunction<Number, Number, Number>) (a, b) -> a.doubleValue() - b.doubleValue());
            context.put("concat", (MVELBiFunction<String, String, String>) (a, b) -> a + b);
            context.put("max", (MVELBiFunction<Number, Number, Number>) (a, b) -> Math.max(a.doubleValue(), b.doubleValue()));
            context.put("min", (MVELBiFunction<Number, Number, Number>) (a, b) -> Math.min(a.doubleValue(), b.doubleValue()));
        */

    }

    public void saveWorkflowFromJsonFile(String name, String workflowJson) {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .name(name)
                .workflowJson(workflowJson)
                .createdAt(LocalDateTime.now())
                .build();
        workflowDefinitionRepository.save(definition);
    }

    public List<WorkflowExecutionDto> getExecutionHistory() {
        List<WorkflowExecution> executions = workflowExecutionRepository.findAll(Sort.by(Sort.Direction.DESC, "executedAt"));
        List<WorkflowExecutionDto> result = new ArrayList<>();

        for (WorkflowExecution exec : executions) {
            List<WorkflowExecutionStep> steps = workflowExecutionStepRepository.findByExecutionOrderByNodeIdAsc(exec);
            List<ExecutionStepDto> stepDtos = steps.stream()
                    .map(step -> ExecutionStepDto.builder()
                            .nodeId(step.getNodeId())
                            .nodeName(step.getNodeName())
                            .requestUrl(step.getRequestUrl())
                            .skipped(step.isSkipped())
                            .statusCode(step.getStatusCode() == 0 ? null : step.getStatusCode())
                            .createdAt(step.getCreatedAt())
                            .build())
                    .toList();

            result.add(WorkflowExecutionDto.builder()
                    .executionId(exec.getExecutionId())
                    .workflowName(exec.getWorkflowName())
                    .executedAt(exec.getExecutedAt())
                    .status(exec.getStatus())
                    .steps(stepDtos)
                    .build());
        }

        return result;
    }


}
