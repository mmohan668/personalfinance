package com.pf.common.repository.financialTransaction;

import com.pf.common.entity.financialTransaction.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
