package com.example.integratedworkflowmanager.service;

import java.util.Map;

public interface WorkflowService {
    Map<String, Object> runWorkflow(String workflowName, Map<String, Object> inputParams);
}

