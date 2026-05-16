package com.studproj.axiom.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkItemTest {

    @Test
    void updateStatusChangesCurrentStatus() {
        WorkItem workItem = WorkItem.builder()
                .status(WorkItemStatus.New)
                .build();

        workItem.updateStatus(WorkItemStatus.Resolved);

        assertThat(workItem.getStatus()).isEqualTo(WorkItemStatus.Resolved);
    }

    @Test
    void assignToChangesAssignee() {
        UUID assigneeId = UUID.randomUUID();
        WorkItem workItem = WorkItem.builder().build();

        workItem.assignTo(assigneeId);

        assertThat(workItem.getAssigneeId()).isEqualTo(assigneeId);
    }
}
