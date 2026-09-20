package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
}
