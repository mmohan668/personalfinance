package com.pf.async.controller;

import com.pf.common.service.financialTransaction.FinancialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/scheduler")
@RequiredArgsConstructor
public class FinancialReportSchedulerController {
    private final FinancialReportService financialReportService;

    @Value("${async.scheduler-token:}")
    private String schedulerApiToken;

    @PostMapping("/dispatch/{today}")
    public void dispatch(
            @RequestHeader(name = "X-Async-Scheduler-Token", required = false) String token,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate today) {
        validateToken(token);
        financialReportService.dispatchReports(today);
    }

    private void validateToken(String token) {
        if (schedulerApiToken.isBlank() || token == null
                || !MessageDigest.isEqual(schedulerApiToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid scheduler credentials");
        }
    }
}
