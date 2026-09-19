package com.pf.common.repository.categoryManagement;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.entity.categoryManagement.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(uc)
            FROM UserCategory uc
            WHERE uc.referenceValue.id = :categoryType
            AND LOWER(TRIM(uc.categoryName)) =LOWER(TRIM(:categoryName))
            AND uc.user.adminUser.id = :userId
            """)
    long countByCategoryTypeAndNameAndUserId(
            @Param("categoryType") Long categoryType,
            @Param("categoryName") String categoryName,
            @Param("userId") Long userId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(uc)
            FROM UserCategory uc
            WHERE uc.referenceValue.id = :categoryType
            AND LOWER(TRIM(uc.categoryName)) =LOWER(TRIM(:categoryName))
            AND uc.user.adminUser.id = :userId
            AND uc.id <> :id
            """)
    long countByCategoryTypeAndNameAndUserIdAndIdNot(
            @Param("categoryType") Long categoryType,
            @Param("categoryName") String categoryName,
            @Param("userId") Long userId,
            @Param("id") Long id
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserCategory uc
            SET uc.active = true, uc.updatedBy = :updatedBy, uc.updatedAt = :updatedAt
            WHERE uc.active = false
            AND uc.id IN (:ids)
            """)
    long activateUserCategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserCategory uc
            SET uc.active = false, uc.updatedBy = :updatedBy, uc.updatedAt = :updatedAt
            WHERE uc.active = true
            AND uc.id IN (:ids)
            """)
    long inactivateUserCategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.dto.generic.SelectItem(
                        uc.id,
                        uc.categoryName
            )
            FROM UserCategory uc
            WHERE uc.user.adminUser.id = :adminUserId
            AND uc.referenceValue.id = :referenceValueId
            ORDER BY LOWER(uc.categoryName)
            """)
    List<SelectItem> fetchCategoriesByUserId(
            @Param("adminUserId") Long adminUserId,
            @Param("referenceValueId") Long referenceValueId
    );
}
