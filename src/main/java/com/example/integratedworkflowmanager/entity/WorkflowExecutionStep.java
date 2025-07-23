package com.example.integratedworkflowmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_execution_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorkflowExecutionStep {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", referencedColumnName = "execution_id", columnDefinition = "CHAR(36)")
    private WorkflowExecution execution;

    private Integer nodeId;

    private String nodeName;

    private String requestUrl;

    @Lob
    private String requestBody;

    @Lob
    private String requestHeaders;

    @Lob
    private String queryParams;

    @Lob
    private String response;

    private int statusCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
