package com.example.integratedworkflowmanager.repository;

import com.example.integratedworkflowmanager.entity.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    boolean existsByWorkflowName(String workflowName);
}

