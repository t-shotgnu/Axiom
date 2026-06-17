package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.dashboard.getdashboardsummary.DashboardSummaryDto;
import com.studproj.axiom.application.features.dashboard.getdashboardsummary.GetDashboardSummaryQuery;
import com.studproj.axiom.application.features.dashboard.getdashboardsummary.GetDashboardSummaryQueryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    private GetDashboardSummaryQueryHandler queryHandler;
    private DashboardController controller;

    @BeforeEach
    void setUp() {
        queryHandler = mock(GetDashboardSummaryQueryHandler.class);
        controller = new DashboardController(queryHandler);
    }

    @Test
    void getSummaryDelegatesOptionalProjectFilter() {
        UUID projectId = UUID.randomUUID();
        DashboardSummaryDto summary = summary(3);
        when(queryHandler.handle(new GetDashboardSummaryQuery(projectId))).thenReturn(summary);

        var response = controller.getSummary(projectId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(summary);
    }

    @Test
    void getSummarySupportsMissingProjectFilter() {
        DashboardSummaryDto summary = summary(0);
        when(queryHandler.handle(new GetDashboardSummaryQuery(null))).thenReturn(summary);

        var response = controller.getSummary(null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(summary);
    }

    private DashboardSummaryDto summary(long activeProjectsCount) {
        return new DashboardSummaryDto(
                activeProjectsCount,
                10,
                5,
                2,
                1,
                3,
                0,
                50,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
