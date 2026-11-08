package com.pf.async.controller;

import com.pf.common.dto.recurringTransaction.SchedulerRecurringTransactionRuleDto;
import com.pf.common.service.financialTransaction.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/recurringTransactions/scheduler")
@RequiredArgsConstructor
public class RecurringTransactionSchedulerController {
    private final RecurringTransactionService recurringTransactionService;

    @Value("${async.scheduler-token:}")
    private String schedulerApiToken;

    @GetMapping("/rules")
    public List<SchedulerRecurringTransactionRuleDto> fetchRules(
            @RequestHeader(name = "X-Async-Scheduler-Token", required = false) String token
    ) {
        validateToken(token);
        return recurringTransactionService.fetchSchedulerRules();
    }

    @PostMapping("/execute/{ruleId}/{occurrenceDate}/{occurrenceTime}")
    public boolean execute(
            @RequestHeader(name = "X-Async-Scheduler-Token", required = false) String token,
            @PathVariable Long ruleId,
            @PathVariable LocalDate occurrenceDate,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime occurrenceTime
    ) {
        validateToken(token);
        return recurringTransactionService.execute(ruleId, occurrenceDate, occurrenceTime);
    }

    private void validateToken(String token) {
        if (schedulerApiToken.isBlank() || token == null
                || !MessageDigest.isEqual(
                schedulerApiToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid scheduler credentials");
        }
    }
}
