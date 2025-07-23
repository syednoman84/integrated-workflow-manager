package com.example.integratedworkflowmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionStepDto {
    private Integer nodeId;
    private String nodeName;
    private String requestUrl;
    private boolean skipped;
    private Integer statusCode;
    private LocalDateTime createdAt;
}

