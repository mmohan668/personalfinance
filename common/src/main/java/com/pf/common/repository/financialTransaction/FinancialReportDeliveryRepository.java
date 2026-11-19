package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.FinancialReportDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FinancialReportDeliveryRepository extends JpaRepository<FinancialReportDelivery, Long> {
    Optional<FinancialReportDelivery> findByAdminUserIdAndFrequencyAndPeriodStartAndPeriodEnd(
            Long adminUserId, String frequency, LocalDate periodStart, LocalDate periodEnd);
}
