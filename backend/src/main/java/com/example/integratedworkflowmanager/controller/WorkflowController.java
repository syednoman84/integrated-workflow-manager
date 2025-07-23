package com.example.integratedworkflowmanager.controller;

import com.example.integratedworkflowmanager.dto.WorkflowExecutionDto;
import com.example.integratedworkflowmanager.entity.WorkflowDefinition;
import com.example.integratedworkflowmanager.entity.WorkflowExecution;
import com.example.integratedworkflowmanager.entity.WorkflowExecutionStep;
import com.example.integratedworkflowmanager.repository.WorkflowDefinitionRepository;
import com.example.integratedworkflowmanager.repository.WorkflowExecutionRepository;
import com.example.integratedworkflowmanager.repository.WorkflowExecutionStepRepository;
import com.example.integratedworkflowmanager.service.WorkflowService;
import com.example.integratedworkflowmanager.util.WorkflowValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowExecutionStepRepository workflowExecutionStepRepository;

    // 🟢 Run a workflow
    @PostMapping("/run/{name}")
    public ResponseEntity<?> runWorkflow(
            @PathVariable String name,
            @RequestBody(required = false) Map<String, Object> input
    ) {
        Map<String, Object> result = workflowService.runWorkflow(name, input == null ? new HashMap<>() : input);
        return ResponseEntity.ok(result);
    }

    // ➕ Add a new workflow
    @PostMapping
    public ResponseEntity<?> addWorkflow(@RequestBody WorkflowDefinition workflow) {
        try {
            if (workflowDefinitionRepository.existsByName(workflow.getName())) {
                return ResponseEntity.badRequest().body("Workflow with this name already exists.");
            }

            // 🔍 Validate JSON structure before saving
            JsonNode workflowJsonNode = objectMapper.readTree(workflow.getWorkflowJson());
            WorkflowValidator.validate(workflowJsonNode);

            workflow.setCreatedAt(LocalDateTime.now());
            workflowDefinitionRepository.save(workflow);

            return ResponseEntity.ok("✅ Workflow added.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("❌ Invalid workflow JSON: " + ex.getMessage());
        }
    }

    // Update existing workflow
    @PutMapping("/{name}")
    public ResponseEntity<?> updateWorkflow(@PathVariable String name, @RequestBody WorkflowDefinition updated) {
        try {
            WorkflowDefinition existing = workflowDefinitionRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + name));

            // 🔍 Validate updated JSON
            JsonNode workflowJsonNode = objectMapper.readTree(updated.getWorkflowJson());
            WorkflowValidator.validate(workflowJsonNode);

            existing.setWorkflowJson(updated.getWorkflowJson());
            workflowDefinitionRepository.save(existing);

            return ResponseEntity.ok("✅ Workflow updated.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("❌ Invalid workflow JSON: " + ex.getMessage());
        }
    }

    // ❌ Delete workflow
    @DeleteMapping("/{name}")
    @Transactional
    public ResponseEntity<?> deleteWorkflow(@PathVariable String name) {
        if (!workflowDefinitionRepository.existsByName(name)) {
            return ResponseEntity.notFound().build();
        }

        boolean isUsedInExecutions = workflowExecutionRepository.existsByWorkflowName(name);
        if (isUsedInExecutions) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ Cannot delete workflow '" + name + "' because it has been used in past executions.");
        }

        workflowDefinitionRepository.deleteByName(name);
        return ResponseEntity.ok("✅ Workflow deleted successfully.");
    }



    // 📄 View single workflow
    @GetMapping("/get/{name}")
    public ResponseEntity<?> getWorkflow(@PathVariable String name) {
        return workflowDefinitionRepository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 📋 View all workflow names
    @GetMapping("/get/all")
    public ResponseEntity<?> listWorkflows() {
        List<String> names = workflowDefinitionRepository.findAll()
                .stream()
                .map(WorkflowDefinition::getName)
                .toList();
        return ResponseEntity.ok(names);
    }

    // Upload workflow via file
    @PostMapping("/fileupload")
    public ResponseEntity<?> uploadWorkflow(@RequestParam("name") String name,
                                            @RequestParam("file") MultipartFile file) {
        try {
            String workflowJson = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 🔍 Validate workflow JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode workflowJsonNode = mapper.readTree(workflowJson);
            WorkflowValidator.validate(workflowJsonNode); // throws if invalid

            // Save after validation
            workflowService.saveWorkflowFromJsonFile(name, workflowJson);

            return ResponseEntity.ok("✅ Workflow uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Invalid workflow JSON: " + e.getMessage());
        }
    }

    // Get all executions
    @GetMapping("/executions")
    public ResponseEntity<List<WorkflowExecutionDto>> getAllExecutions() {
        List<WorkflowExecutionDto> executions = workflowService.getExecutionHistory();
        return ResponseEntity.ok(executions);
    }

    // Get single execution
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<?> getExecutionById(@PathVariable UUID executionId) {
        Optional<WorkflowExecution> optionalExecution = workflowExecutionRepository.findById(executionId);
        if (optionalExecution.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ No execution found with ID: " + executionId);
        }

        WorkflowExecution execution = optionalExecution.get();
        List<WorkflowExecutionStep> steps = workflowExecutionStepRepository.findByExecution(execution);

        Map<String, Object> result = new HashMap<>();
        result.put("executionId", execution.getExecutionId());
        result.put("workflowName", execution.getWorkflowName());
        result.put("executedAt", execution.getExecutedAt());
        result.put("status", execution.getStatus());

        List<Map<String, Object>> stepDetails = steps.stream().map(step -> {
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("nodeId", step.getNodeId());
            stepMap.put("nodeName", step.getNodeName());
            stepMap.put("requestUrl", step.getRequestUrl());
            stepMap.put("requestBody", step.getRequestBody());
            stepMap.put("requestHeaders", step.getRequestHeaders());
            stepMap.put("queryParams", step.getQueryParams());
            stepMap.put("response", step.getResponse());
            stepMap.put("statusCode", step.getStatusCode());
            stepMap.put("skipped", step.isSkipped());
            return stepMap;
        }).toList();

        result.put("steps", stepDetails);

        return ResponseEntity.ok(result);
    }


}
