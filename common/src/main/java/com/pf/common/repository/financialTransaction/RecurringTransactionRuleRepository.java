package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.RecurringTransactionRule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRuleRepository extends JpaRepository<RecurringTransactionRule, Long> {
    @Transactional(readOnly = true)
    List<RecurringTransactionRule> findByAdminUser_IdOrderByIdDesc(Long adminUserId);

    @Transactional(readOnly = true)
    @Query("""
            SELECT r
            FROM RecurringTransactionRule r
            JOIN r.subcategory s
            JOIN s.userCategory c
            LEFT JOIN r.location l
            WHERE r.active = true
              AND s.active = true
              AND c.active = true
              AND (l IS NULL OR l.active = true)
            ORDER BY r.id
            """)
    List<RecurringTransactionRule> findRunnableRules();

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(r) > 0
            FROM RecurringTransactionRule r
            WHERE r.subcategory.userCategory.id IN :categoryIds
            AND r.active = true
            AND (r.endDate IS NULL OR r.endDate >= :currentDate)
            """)
    boolean existsByCategoryIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("currentDate") LocalDate currentDate
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(r) > 0
            FROM RecurringTransactionRule r
            WHERE r.subcategory.id IN :categoryIds
            AND r.active = true
            AND (r.endDate IS NULL OR r.endDate >= :currentDate)
            """)
    boolean existsBySubCategoryIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("currentDate") LocalDate currentDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    @Query("SELECT r FROM RecurringTransactionRule r WHERE r.id = :id")
    Optional<RecurringTransactionRule> findByIdForUpdate(@Param("id") Long id);
}
