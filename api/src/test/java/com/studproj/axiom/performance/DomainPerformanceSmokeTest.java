package com.studproj.axiom.performance;

import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.infrastructure.persistence.mapper.WorkItemMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class DomainPerformanceSmokeTest {

    @Test
    void allowedChildTypesCanBeComputedRepeatedlyWithinSmokeBudget() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int total = 0;
            for (int i = 0; i < 25_000; i++) {
                for (WorkItemType type : WorkItemType.values()) {
                    total += type.allowedChildTypes().size();
                }
            }
            assertThat(total).isEqualTo(375_000);
        });
    }

    @Test
    void workItemMappingRoundTripsLargeBatchesWithinSmokeBudget() {
        WorkItem workItem = WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(1)
                .description("Performance smoke")
                .priority(2)
                .type(WorkItemType.Task)
                .status(WorkItemStatus.Active)
                .dueDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .estimatedEffort(5)
                .projectId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .assigneeId(UUID.randomUUID())
                .notes("Mapping should stay tiny")
                .build();

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            WorkItem mapped = workItem;
            for (int i = 0; i < 50_000; i++) {
                mapped = WorkItemMapper.toDomain(WorkItemMapper.toEntity(mapped));
            }
            assertThat(mapped.getDescription()).isEqualTo("Performance smoke");
            assertThat(mapped.getStatus()).isEqualTo(WorkItemStatus.Active);
        });
    }
}
