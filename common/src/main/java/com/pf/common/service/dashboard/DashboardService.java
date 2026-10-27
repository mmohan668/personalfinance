package com.pf.common.service.dashboard;

import com.pf.common.dto.dashboard.DashboardSummary;
import com.pf.common.projection.ExpenseByCategoryProjection;
import com.pf.common.projection.TransactionTotalProjection;
import com.pf.common.repository.financialTransaction.FinancialTransactionRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService extends BaseService {
    private final FinancialTransactionRepository financialTransactionRepository;

    public DashboardSummary fetchDashboardSummary(int year, int month) {
        log.info("Fetching Dashboard Summary for year {} and month {}", year, month);
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = LocalDate.now().plusMonths(1);
        List<TransactionTotalProjection> totalProjections = financialTransactionRepository.fetchTotals(from, to);
        List<ExpenseByCategoryProjection> expenseProjections = financialTransactionRepository.fetchExpenseByCategory(from, to);
        log.info("Completed Fetching Dashboard Summary for year {} and month {}", year, month);
        return new DashboardSummary(totalProjections, expenseProjections);
    }
}
