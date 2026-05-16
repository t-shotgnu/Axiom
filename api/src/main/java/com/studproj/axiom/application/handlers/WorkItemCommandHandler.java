package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateWorkItemCommand;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public UUID createWorkItem(CreateWorkItemCommand command) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID authorId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();

        int nextControlNo = workItemRepository
                .findMaxControlNoByProjectId(command.projectId())
                .map(max -> max + 1)
                .orElse(1);

        WorkItem workItem = WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(nextControlNo)
                .description(command.description())
                .priority(command.priority())
                .type(command.type())
                .status(command.status())
                .dueDate(command.dueDate())
                .estimatedEffort(command.estimatedEffort())
                .projectId(command.projectId())
                .authorId(authorId)
                .assigneeId(command.assigneeId())
                .build();

        workItemRepository.save(workItem);
        return workItem.getId();
    }

    @Transactional
    public void assignWorkItem(UUID id, UUID assigneeId) {
        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkItem not found"));
        workItem.assignTo(assigneeId);
        workItemRepository.save(workItem);
    }

    @Transactional
    public void updateStatus(UUID id, com.studproj.axiom.domain.model.WorkItemStatus status) {
        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkItem not found"));
        workItem.updateStatus(status);
        workItemRepository.save(workItem);
    }

    @Transactional
    public void updateNotes(UUID id, String notes) {
        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkItem not found"));
        workItem.updateNotes(notes);
        workItemRepository.save(workItem);
    }
}

