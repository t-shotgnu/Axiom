package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.dashboard.getdashboardsummary.DashboardSummaryDto;
import com.studproj.axiom.application.features.dashboard.getdashboardsummary.GetDashboardSummaryQuery;
import com.studproj.axiom.application.features.dashboard.getdashboardsummary.GetDashboardSummaryQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final GetDashboardSummaryQueryHandler queryHandler;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(@RequestParam(required = false) UUID projectId) {
        return ResponseEntity.ok(queryHandler.handle(new GetDashboardSummaryQuery(projectId)));
    }
}
