package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateWorkItemCommand;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;

    @Transactional
    public UUID createWorkItem(CreateWorkItemCommand command) {
        WorkItem workItem = WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(command.controlNo())
                .description(command.description())
                .priority(command.priority())
                .type(command.type())
                .status(command.status())
                .dueDate(command.dueDate())
                .estimatedEffort(command.estimatedEffort())
                .projectId(command.projectId())
                .authorId(command.authorId())
                .assigneeId(command.assigneeId())
                .build();

        workItemRepository.save(workItem);
        return workItem.getId();
    }
}

