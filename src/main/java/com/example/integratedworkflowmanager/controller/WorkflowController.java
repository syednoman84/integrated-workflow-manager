package com.example.integratedworkflowmanager.controller;

import com.example.integratedworkflowmanager.entity.WorkflowDefinition;
import com.example.integratedworkflowmanager.repository.WorkflowDefinitionRepository;
import com.example.integratedworkflowmanager.service.WorkflowService;
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
        if (workflowRepo.existsByName(workflow.getName())) {
            return ResponseEntity.badRequest().body("Workflow with this name already exists.");
        }
        workflow.setCreatedAt(LocalDateTime.now());
        workflowRepo.save(workflow);
        return ResponseEntity.ok("Workflow added.");
    }

    // 🔁 Update existing workflow
    @PutMapping("/{name}")
    public ResponseEntity<?> updateWorkflow(@PathVariable String name, @RequestBody WorkflowDefinition updated) {
        WorkflowDefinition existing = workflowRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + name));
        existing.setWorkflowJson(updated.getWorkflowJson());
        workflowRepo.save(existing);
        return ResponseEntity.ok("Workflow updated.");
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
            workflowService.saveWorkflowFromJsonFile(name, workflowJson);
            return ResponseEntity.ok("✅ Workflow uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Failed to upload workflow: " + e.getMessage());
        }
    }
}
