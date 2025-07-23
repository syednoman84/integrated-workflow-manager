package com.example.integratedworkflowmanager.repository;

import com.example.integratedworkflowmanager.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {
    Optional<WorkflowDefinition> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
