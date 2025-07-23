package com.example.integratedworkflowmanager.repository;

import com.example.integratedworkflowmanager.entity.WorkflowErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowErrorLogRepository extends JpaRepository<WorkflowErrorLog, UUID> {
}
