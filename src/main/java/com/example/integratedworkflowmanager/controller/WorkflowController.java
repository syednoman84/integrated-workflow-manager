package com.example.integratedworkflowmanager.controller;

import com.example.integratedworkflowmanager.dto.WorkflowExecutionDto;
import com.example.integratedworkflowmanager.entity.WorkflowDefinition;
import com.example.integratedworkflowmanager.repository.WorkflowDefinitionRepository;
import com.example.integratedworkflowmanager.service.WorkflowService;
import com.example.integratedworkflowmanager.util.WorkflowValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
    private final WorkflowDefinitionRepository workflowRepo;
    private final ObjectMapper objectMapper;

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
            if (workflowRepo.existsByName(workflow.getName())) {
                return ResponseEntity.badRequest().body("Workflow with this name already exists.");
            }

            // 🔍 Validate JSON structure before saving
            JsonNode workflowJsonNode = objectMapper.readTree(workflow.getWorkflowJson());
            WorkflowValidator.validate(workflowJsonNode);

            workflow.setCreatedAt(LocalDateTime.now());
            workflowRepo.save(workflow);

            return ResponseEntity.ok("✅ Workflow added.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("❌ Invalid workflow JSON: " + ex.getMessage());
        }
    }

    // Update existing workflow
    @PutMapping("/{name}")
    public ResponseEntity<?> updateWorkflow(@PathVariable String name, @RequestBody WorkflowDefinition updated) {
        try {
            WorkflowDefinition existing = workflowRepo.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + name));

            // 🔍 Validate updated JSON
            JsonNode workflowJsonNode = objectMapper.readTree(updated.getWorkflowJson());
            WorkflowValidator.validate(workflowJsonNode);

            existing.setWorkflowJson(updated.getWorkflowJson());
            workflowRepo.save(existing);

            return ResponseEntity.ok("✅ Workflow updated.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("❌ Invalid workflow JSON: " + ex.getMessage());
        }
    }

    // ❌ Delete workflow
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String name) {
        if (!workflowRepo.existsByName(name)) {
            return ResponseEntity.notFound().build();
        }
        workflowRepo.deleteByName(name);
        return ResponseEntity.ok("Workflow deleted.");
    }

    // 📄 View single workflow
    @GetMapping("/{name}")
    public ResponseEntity<?> getWorkflow(@PathVariable String name) {
        return workflowRepo.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 📋 View all workflow names
    @GetMapping
    public ResponseEntity<?> listWorkflows() {
        List<String> names = workflowRepo.findAll()
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

    @GetMapping("/executions")
    public ResponseEntity<List<WorkflowExecutionDto>> getAllExecutions() {
        List<WorkflowExecutionDto> executions = workflowService.getExecutionHistory();
        return ResponseEntity.ok(executions);
    }

}
