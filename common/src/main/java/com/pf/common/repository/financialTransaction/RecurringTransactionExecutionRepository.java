package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.RecurringTransactionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface RecurringTransactionExecutionRepository extends JpaRepository<RecurringTransactionExecution, Long> {
    @Transactional(readOnly = true)
    boolean existsByRule_IdAndOccurrenceDateAndOccurrenceTime(Long ruleId, LocalDate occurrenceDate, LocalTime occurrenceTime);

    @Transactional(readOnly = true)
    Optional<RecurringTransactionExecution> findFirstByRule_IdOrderByOccurrenceDateDescOccurrenceTimeDesc(Long ruleId);

}
