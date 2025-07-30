package com.example.integratedworkflowmanager.entity;

import jakarta.persistence.*;
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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private LocalDateTime timestamp;
}
