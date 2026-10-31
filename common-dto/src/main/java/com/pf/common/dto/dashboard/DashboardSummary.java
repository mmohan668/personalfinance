package com.pf.common.dto.dashboard;

import com.pf.common.projection.ExpenseByCategoryProjection;
import com.pf.common.projection.MonthlyTransactionTotalProjection;
import com.pf.common.projection.TransactionTotalProjection;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummary {
    TotalByTransactionType transactions = new TotalByTransactionType();
    List<MonthlyTransactionTotalProjection> monthlyTransactions = new ArrayList<>();
    List<ExpenseByCategory> expenses = new ArrayList<>();

    public DashboardSummary(List<TransactionTotalProjection> transactions, List<ExpenseByCategoryProjection> expenses, List<MonthlyTransactionTotalProjection> monthlyTransactions) {
        for (TransactionTotalProjection transaction : transactions) {
            BigDecimal amount = transaction.getTotal() != null
                    ? transaction.getTotal()
                    : BigDecimal.ZERO;
            switch (transaction.getTransactionType().toUpperCase()) {
                case "EXPENSE" -> this.transactions.setExpense(amount);
                case "INCOME" -> this.transactions.setIncome(amount);
                case "INVESTMENT" -> this.transactions.setInvestment(amount);
                case "TRANSFER" -> this.transactions.setTransfer(amount);
            }
        }
        for (ExpenseByCategoryProjection expenseByCategoryProjection : expenses) {
            ExpenseByCategory expenseByCategory = new ExpenseByCategory();
            expenseByCategory.setAmount(expenseByCategoryProjection.getAmount() != null
                    ? expenseByCategoryProjection.getAmount()
                    : BigDecimal.ZERO);
            expenseByCategory.setCategoryName(expenseByCategoryProjection.getCategoryName());
            this.expenses.add(expenseByCategory);
        }
        this.monthlyTransactions.addAll(monthlyTransactions);
    }
}
