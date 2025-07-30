package com.example.integratedworkflowmanager.repository;

import com.example.integratedworkflowmanager.entity.WorkflowExecution;
import com.example.integratedworkflowmanager.entity.WorkflowExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionStepRepository extends JpaRepository<WorkflowExecutionStep, UUID> {
    List<WorkflowExecutionStep> findByExecutionOrderByNodeIdAsc(WorkflowExecution execution);
    boolean existsByApplicationIdAndExecution_WorkflowNameAndNodeNameAndSkippedFalse(
            String applicationId,
            String workflowName,
            String nodeName
    );

}
