package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.RecurringTransactionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionExecutionRepository extends JpaRepository<RecurringTransactionExecution, Long> {
    @Transactional(readOnly = true)
    boolean existsByRule_IdAndOccurrenceDateAndOccurrenceTime(Long ruleId, LocalDate occurrenceDate, LocalTime occurrenceTime);

    @Transactional(readOnly = true)
    Optional<RecurringTransactionExecution> findFirstByRule_IdOrderByOccurrenceDateDescOccurrenceTimeDesc(Long ruleId);

    @Transactional(readOnly = true)
    @Query("""
            SELECT e.occurrenceDate
            FROM RecurringTransactionExecution e
            WHERE e.rule.id = :ruleId
            ORDER BY e.occurrenceDate
            """)
    List<LocalDate> findExecutedDates(@Param("ruleId") Long ruleId);
}
