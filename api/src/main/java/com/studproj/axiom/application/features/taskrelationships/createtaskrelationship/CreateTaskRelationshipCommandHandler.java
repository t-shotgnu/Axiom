package com.studproj.axiom.application.features.taskrelationships.createtaskrelationship;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.LinkType;
import com.studproj.axiom.domain.model.TaskRelationship;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.TaskRelationshipRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTaskRelationshipCommandHandler {
    private final TaskRelationshipRepository taskRelationshipRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID handle(CreateTaskRelationshipCommand command) {
        // Normalization
        LinkType normalizedLinkType = command.linkType();
        UUID normalizedSourceId = command.sourceId();
        UUID normalizedTargetId = command.targetId();

        if (normalizedLinkType == LinkType.ParentOf) {
            normalizedLinkType = LinkType.ChildOf;
            normalizedSourceId = command.targetId();
            normalizedTargetId = command.sourceId();
        } else if (normalizedLinkType == LinkType.Blocks) {
            normalizedLinkType = LinkType.BlockedBy;
            normalizedSourceId = command.targetId();
            normalizedTargetId = command.sourceId();
        }

        // Self-linking check
        if (normalizedSourceId.equals(normalizedTargetId)) {
            throw new DomainRuleViolationException("A work item cannot be linked to itself");
        }

        // Existence check
        WorkItem sourceTask = workItemRepository.findById(normalizedSourceId)
                .orElseThrow(() -> new EntityNotFoundException("Source task not found"));
        WorkItem targetTask = workItemRepository.findById(normalizedTargetId)
                .orElseThrow(() -> new EntityNotFoundException("Target task not found"));

        // Same project check
        if (!sourceTask.getProjectId().equals(targetTask.getProjectId())) {
            throw new DomainRuleViolationException("Tasks must belong to the same project");
        }

        // Access check
        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                sourceTask.getProjectId()
        );

        // Check duplicates
        Optional<TaskRelationship> existing = taskRelationshipRepository.findBySourceIdAndTargetIdAndLinkType(
                normalizedSourceId, normalizedTargetId, normalizedLinkType);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        if (normalizedLinkType == LinkType.ChildOf) {
            // Parent-child rules:
            // sourceId = child, targetId = parent
            WorkItemType parentType = targetTask.getType();
            WorkItemType childType = sourceTask.getType();

            boolean isValid = false;
            if (parentType == WorkItemType.Subtask && childType == WorkItemType.Subtask) {
                isValid = true;
            } else {
                isValid = parentType.level() < childType.level();
            }

            if (!isValid) {
                throw new DomainRuleViolationException("Invalid parent-child relationship hierarchy: " + parentType + " cannot parent " + childType);
            }

            // A task can have at most one parent. Delete existing parent relationship
            List<TaskRelationship> existingParentLinks = taskRelationshipRepository.findBySourceIdAndLinkType(normalizedSourceId, LinkType.ChildOf);
            for (TaskRelationship link : existingParentLinks) {
                taskRelationshipRepository.delete(link.getId());
            }

            // Cycle detection (Linear ancestors check)
            UUID currentParentId = normalizedTargetId;
            while (currentParentId != null) {
                if (currentParentId.equals(normalizedSourceId)) {
                    throw new DomainRuleViolationException("Cyclic parent-child relationship detected.");
                }
                List<TaskRelationship> parentLinks = taskRelationshipRepository.findBySourceIdAndLinkType(currentParentId, LinkType.ChildOf);
                if (parentLinks.isEmpty()) {
                    currentParentId = null;
                } else {
                    currentParentId = parentLinks.get(0).getTargetId();
                }
            }
        }

        // Dependency cycle check
        if (normalizedLinkType == LinkType.BlockedBy) {
            if (isDependencyReachable(normalizedTargetId, normalizedSourceId, new HashSet<>())) {
                throw new DomainRuleViolationException("Cyclic dependency relationship detected.");
            }
        }

        // Save new relationship
        TaskRelationship newRelationship = TaskRelationship.builder()
                .id(UUID.randomUUID())
                .sourceId(normalizedSourceId)
                .targetId(normalizedTargetId)
                .linkType(normalizedLinkType)
                .build();

        taskRelationshipRepository.save(newRelationship);
        return newRelationship.getId();
    }


    private boolean isDependencyReachable(UUID currentId, UUID targetId, Set<UUID> visited) {
        if (currentId.equals(targetId)) return true;
        if (!visited.add(currentId)) return false;
        List<TaskRelationship> deps = taskRelationshipRepository.findBySourceIdAndLinkType(currentId, LinkType.BlockedBy);
        for (TaskRelationship dep : deps) {
            if (isDependencyReachable(dep.getTargetId(), targetId, visited)) {
                return true;
            }
        }
        return false;
    }
}
