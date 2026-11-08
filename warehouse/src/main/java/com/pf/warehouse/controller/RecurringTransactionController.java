package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.recurringTransaction.RecurringTransactionRuleDto;
import com.pf.common.service.financialTransaction.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recurringTransactions")
@RequiredArgsConstructor
public class RecurringTransactionController {
    private final RecurringTransactionService service;

    @GetMapping
    public List<RecurringTransactionRuleDto> fetchRules() {
        return service.fetchRules();
    }

    @PostMapping("/fetchRecurringTransactionsGridData")
    public GridResult fetchGridData(@RequestBody SearchCriteria criteria) {
        return service.fetchGridData(criteria);
    }

    @PostMapping("/delete")
    public ApiResponse deleteRules(@RequestBody List<Long> ruleIds) {
        return service.deleteRules(ruleIds);
    }

    @PostMapping("/setActive")
    public ApiResponse setRulesActive(
            @RequestBody RuleStatusRequest request
    ) {
        return service.setRulesActive(request.ruleIds(), request.active());
    }

    @PostMapping("/save")
    public ApiResponse saveRule(@RequestBody RecurringTransactionRuleDto dto) {
        return service.saveRule(dto);
    }

    public record RuleStatusRequest(List<Long> ruleIds, boolean active) {
    }
}
