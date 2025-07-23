package com.example.integratedworkflowmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowExecutionDto {
    private UUID executionId;
    private String workflowName;
    private LocalDateTime executedAt;
    private String status;
    private List<ExecutionStepDto> steps;
}