package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.response.DashboardSummaryResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getSummary(user));
    }
}
