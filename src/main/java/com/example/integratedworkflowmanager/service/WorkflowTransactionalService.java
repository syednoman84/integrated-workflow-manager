package com.example.integratedworkflowmanager.service;

import com.example.integratedworkflowmanager.entity.WorkflowErrorLog;
import com.example.integratedworkflowmanager.entity.WorkflowExecution;
import com.example.integratedworkflowmanager.entity.WorkflowExecutionStep;
import com.example.integratedworkflowmanager.repository.WorkflowErrorLogRepository;
import com.example.integratedworkflowmanager.repository.WorkflowExecutionRepository;
import com.example.integratedworkflowmanager.repository.WorkflowExecutionStepRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowTransactionalService {

    private final WorkflowExecutionRepository executionRepo;
    private final WorkflowExecutionStepRepository stepRepo;
    private final WorkflowErrorLogRepository errorRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public WorkflowExecution saveWorkflowExecution(WorkflowExecution execution) {
        return executionRepo.saveAndFlush(execution);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateWorkflowStatus(UUID executionId, String status) {
        executionRepo.findById(executionId).ifPresent(exec -> {
            exec.setStatus(status);
            executionRepo.save(exec);
        });
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveWorkflowStep(
            WorkflowExecution execution, Integer nodeId, String nodeName, String url,
            Map<String, Object> body, Map<String, Object> headers, Map<String, Object> query,
            String response, int statusCode
    ) {
        try {
            WorkflowExecutionStep step = WorkflowExecutionStep.builder()
                    .execution(execution)
                    .nodeId(nodeId)
                    .nodeName(nodeName)
                    .requestUrl(url)
                    .requestBody(objectMapper.writeValueAsString(body))
                    .requestHeaders(objectMapper.writeValueAsString(headers))
                    .queryParams(objectMapper.writeValueAsString(query))
                    .response(response)
                    .statusCode(statusCode)
                    .build();
            stepRepo.save(step);
        } catch (Exception e) {
            // log internally, don't fail the flow
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveWorkflowError(UUID executionId, String workflowName, String errorMsg) {
        errorRepo.save(WorkflowErrorLog.builder()
                .executionId(executionId)
                .workflowName(workflowName)
                .errorMessage(errorMsg)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
