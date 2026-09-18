package com.pf.common.repository.categoryManagement;

import com.pf.common.entity.categoryManagement.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

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
}
