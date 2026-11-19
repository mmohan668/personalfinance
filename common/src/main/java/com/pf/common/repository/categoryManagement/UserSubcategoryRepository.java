package com.pf.common.repository.categoryManagement;

import com.pf.common.record.generic.SelectItem;
import com.pf.common.projection.InvoiceCategoryOption;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSubcategoryRepository extends JpaRepository<UserSubcategory, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_categories
                (user_id, transaction_type, category_name, category_description, is_active, created_by)
            SELECT urv.user_id, urv.id, 'Miscellaneous', 'Expenses not matched to another category', TRUE, :createdBy
            FROM user_reference_value urv
            JOIN reference_object ro ON ro.id = urv.ref_obj_name_id
            WHERE urv.user_id = :adminUserId
              AND LOWER(TRIM(ro.ref_obj_name)) = LOWER(TRIM('TRANSACTION_TYPE'))
              AND LOWER(TRIM(urv.reference_code)) = LOWER(TRIM('EXPENSE'))
              AND urv.is_active = TRUE
              AND urv.is_deleted = FALSE
            ON CONFLICT (user_id, transaction_type, (LOWER(TRIM(category_name)))) WHERE is_deleted = FALSE
            DO UPDATE SET is_active = TRUE, updated_by = :createdBy, updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int ensureMiscellaneousExpenseCategory(
            @Param("adminUserId") Long adminUserId,
            @Param("createdBy") Long createdBy
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_subcategories
                (user_category_id, subcategory_name, subcategory_description, is_active, created_by)
            SELECT uc.id, 'Miscellaneous', 'Other expenses in this category', TRUE, :createdBy
            FROM user_categories uc
            JOIN user_reference_value urv ON urv.id = uc.transaction_type AND urv.user_id = uc.user_id
            WHERE uc.user_id = :adminUserId
              AND LOWER(TRIM(urv.reference_code)) = LOWER(TRIM('EXPENSE'))
              AND urv.is_active = TRUE
              AND urv.is_deleted = FALSE
              AND uc.is_active = TRUE
              AND uc.is_deleted = FALSE
            ON CONFLICT (user_category_id, (LOWER(TRIM(subcategory_name)))) WHERE is_deleted = FALSE
            DO UPDATE SET is_active = TRUE, updated_by = :createdBy, updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int ensureMiscellaneousSubcategoryForExpenseCategories(
            @Param("adminUserId") Long adminUserId,
            @Param("createdBy") Long createdBy
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT uc.id AS categoryId,
                   uc.categoryName AS categoryName,
                   usc.id AS subcategoryId,
                   usc.subcategoryName AS subcategoryName,
                   usc.subcategoryDescription AS subcategoryDescription
            FROM UserSubcategory usc
            JOIN usc.userCategory uc
            WHERE uc.user.id = :adminUserId
              AND uc.userReferenceValue.referenceCode = 'EXPENSE'
              AND uc.userReferenceValue.active = true
              AND uc.userReferenceValue.isDeleted = false
              AND uc.active = true
              AND uc.isDeleted = false
              AND usc.active = true
              AND usc.isDeleted = false
            ORDER BY LOWER(uc.categoryName), LOWER(usc.subcategoryName)
            """)
    List<InvoiceCategoryOption> findActiveExpenseCategoryOptions(@Param("adminUserId") Long adminUserId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = true, usc.updatedBy.id = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.active = false
            AND usc.userCategory.id IN (:ids)
            """)
    int activateSubcategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = false, usc.updatedBy.id = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.active = true
            AND usc.userCategory.id IN (:ids)
            """)
    int inactivateSubcategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(usc) > 0
            FROM UserSubcategory usc
            WHERE usc.userCategory.id = :categoryId
            AND LOWER(TRIM(usc.subcategoryName)) = LOWER(TRIM(:subcategoryName))
            AND usc.isDeleted = false
            """)
    boolean existsByCategoryIdAndSubcategoryName(
            @Param("categoryId") Long categoryId,
            @Param("subcategoryName") String subcategoryName
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(usc) > 0
            FROM UserSubcategory usc
            WHERE usc.userCategory.id = :categoryId
            AND LOWER(TRIM(usc.subcategoryName)) = LOWER(TRIM(:subcategoryName))
            AND usc.id <> :subcategoryId
            AND usc.isDeleted = false
            """)
    boolean existsByCategoryIdAndSubcategoryNameAndIdNot(
            @Param("categoryId") Long categoryId,
            @Param("subcategoryName") String subcategoryName,
            @Param("subcategoryId") Long subcategoryId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = true, usc.updatedBy.id = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.id IN (:ids)
            AND usc.active = false
            """)
    int activateSubcategories(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = false, usc.updatedBy.id = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.id IN (:ids)
            AND usc.active = true
            """)
    int inactivateSubcategories(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.record.generic.SelectItem(
                        usc.id,
                        usc.subcategoryName
            )
            FROM UserSubcategory usc
            WHERE usc.userCategory.id = :categoryId
            AND usc.active = true
            AND usc.isDeleted = false
            ORDER BY LOWER(usc.subcategoryName)
            """)
    List<SelectItem> fetchSubcategoriesByCategory(Long categoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.isDeleted = true, usc.deletedBy.id = :deletedBy, usc.deletedAt = :deletedAt
            WHERE usc.id IN (:ids)
            """)
    void softDeleteSubcategories(
            @Param("ids") List<Long> ids,
            @Param("deletedBy") Long deletedBy,
            @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.isDeleted = true, usc.deletedBy.id = :deletedBy, usc.deletedAt = :deletedAt
            WHERE usc.userCategory.id IN (:ids)
            """)
    void softDeleteSubcategoriesByCategoryIds(
            @Param("ids") List<Long> ids,
            @Param("deletedBy") Long deletedBy,
            @Param("deletedAt") LocalDateTime deletedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM UserSubcategory usc
            WHERE usc.userCategory.id IN (:ids)
            """)
    void deleteByCategoryId(@Param("ids") List<Long> ids);
}
