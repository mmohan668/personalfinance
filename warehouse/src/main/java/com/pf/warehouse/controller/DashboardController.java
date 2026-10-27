package com.pf.warehouse.controller;

import com.pf.common.dto.dashboard.DashboardSummary;
import com.pf.common.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/fetchDashboardSummary")
    public DashboardSummary fetchDashboardSummary(@RequestParam int year, @RequestParam int month) {
        return dashboardService.fetchDashboardSummary(year, month);
    }
}
