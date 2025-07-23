package com.example.integratedworkflowmanager.service;

import com.example.integratedworkflowmanager.dto.WorkflowExecutionDto;

import java.util.List;
import java.util.Map;

public interface WorkflowService {
    Map<String, Object> runWorkflow(String workflowName, Map<String, Object> inputParams);
    void saveWorkflowFromJsonFile(String name, String workflowJson);
    List<WorkflowExecutionDto> getExecutionHistory();
}

