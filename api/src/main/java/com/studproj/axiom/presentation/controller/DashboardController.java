package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.query.DashboardSummaryDto;
import com.studproj.axiom.application.handlers.DashboardQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardQueryHandler queryHandler;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(queryHandler.getSummary());
    }
}
