package com.example.integratedworkflowmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorkflowErrorLog {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID executionId;

    private String workflowName;

    private String errorMessage;

    private LocalDateTime timestamp;
}
