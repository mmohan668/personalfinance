package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.FinancialTransaction;
import com.pf.common.projection.ExpenseByCategoryProjection;
import com.pf.common.projection.MonthlyTransactionTotalProjection;
import com.pf.common.projection.TransactionTotalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(ft) > 0
              FROM FinancialTransaction ft
             WHERE ft.subcategory.id IN (:ids)
            """)
    boolean existsBySubcategoryIds(@Param("ids") List<Long> subcategoryIds);


    @Transactional(readOnly = true)
    @Query("""
              SELECT ft
                FROM FinancialTransaction ft
               WHERE ft.subcategory.id = :id
                 AND ft.transactionAt = :date
                 AND (ft.location.id = :location OR (ft.location.id IS NULL AND :location IS NULL))
                 AND ((ft.remarks IS NULL AND :remarks IS NULL) OR LOWER(TRIM(ft.remarks)) = :remarks)
                 AND ft.adminUser.id = :userId
            ORDER BY ft.createdAt DESC FETCH FIRST 1 ROWS ONLY
            """)
    FinancialTransaction findIdBySameCombination(
            @Param("id") Long subcategoryId,
            @Param("date") LocalDate date,
            @Param("location") Long location,
            @Param("remarks") String remarks,
            @Param("userId") Long userId
    );

    @Transactional(readOnly = true)
    @Query("""
                SELECT COALESCE(SUM(ftv.amount), 0) AS total,
                       ftv.transactionType AS transactionType
                FROM FinancialTransactionView ftv
                WHERE ftv.transactionAt >= :from
                AND ftv.transactionAt < :to
                AND ftv.adminUserId = :adminUserId
                GROUP BY ftv.transactionType
            """)
    List<TransactionTotalProjection> fetchTransactionTotalProjection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("adminUserId") Long adminUserId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT
                YEAR(ftv.transactionAt) AS transactionYear,
                MONTH(ftv.transactionAt) AS transactionMonth,
                ftv.transactionType AS transactionType,
                COALESCE(SUM(ftv.amount), 0) AS total
            FROM FinancialTransactionView ftv
            WHERE ftv.transactionAt >= :from
              AND ftv.transactionAt < :to
              AND ftv.adminUserId = :adminUserId
            GROUP BY
                YEAR(ftv.transactionAt),
                MONTH(ftv.transactionAt),
                ftv.transactionType
            ORDER BY
                transactionYear,
                transactionMonth
            """)
    List<MonthlyTransactionTotalProjection> fetchMonthlyTransactionTotalProjection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("adminUserId") Long adminUserId
    );

    @Transactional(readOnly = true)
    @Query("""
                SELECT COALESCE(SUM(ftv.amount), 0) AS amount,
                       ftv.categoryName AS categoryName
                FROM FinancialTransactionView ftv
                WHERE ftv.transactionAt >= :from
                AND ftv.transactionAt < :to
                AND ftv.transactionType = 'EXPENSE'
                AND ftv.adminUserId = :adminUserId
                GROUP BY ftv.categoryName
            """)
    List<ExpenseByCategoryProjection> fetchExpenseByCategory(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("adminUserId") Long adminUserId
    );

}
