package com.pf.warehouse.controller;

import com.pf.common.dto.financialTransaction.FinancialTransactionDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.service.financialTransaction.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/financialTransaction")
public class FinancialTransactionController {
    private final FinancialTransactionService financialTransactionService;

    @PostMapping("/fetchExpensesGridData")
    public GridResult fetchExpensesGridData(@RequestBody SearchCriteria searchCriteria) {
        return financialTransactionService.fetchExpensesGridData(searchCriteria);
    }

    @PostMapping("/saveFinancialTransaction")
    public ApiResponse saveFinancialTransaction(@RequestBody FinancialTransactionDto financialTransactionDto) {
        return financialTransactionService.saveFinancialTransaction(financialTransactionDto);
    }
}
