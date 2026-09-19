package com.pf.common.repository.categoryManagement;

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

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(usc)
            FROM UserSubcategory usc
            WHERE usc.userCategory.id IN (:ids)
            """)
    long countByCategoryId(@Param("ids") List<Long> ids);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = true, usc.updatedBy = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.active = false
            AND usc.userCategory.id IN (:ids)
            """)
    long activateSubcategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = false, usc.updatedBy = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.active = true
            AND usc.userCategory.id IN (:ids)
            """)
    long inactivateSubcategory(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(usc)
            FROM UserSubcategory usc
            WHERE usc.userCategory.id = :categoryId
            AND LOWER(TRIM(usc.subcategoryName)) = LOWER(TRIM(:subcategoryName))
            """)
    long countByCategoryIdAndSubcategoryName(
            @Param("categoryId") Long categoryId,
            @Param("subcategoryName") String subcategoryName
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(usc)
            FROM UserSubcategory usc
            WHERE usc.userCategory.id = :categoryId
            AND LOWER(TRIM(usc.subcategoryName)) = LOWER(TRIM(:subcategoryName))
            AND usc.id <> :subcategoryId
            """)
    long countByCategoryIdAndSubcategoryNameAndIdNot(
            @Param("categoryId") Long categoryId,
            @Param("subcategoryName") String subcategoryName,
            @Param("subcategoryId") Long subcategoryId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = true, usc.updatedBy = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.id IN (:ids)
            AND usc.active = false
            """)
    long activateSubcategories(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSubcategory usc
            SET usc.active = false, usc.updatedBy = :updatedBy, usc.updatedAt = :updatedAt
            WHERE usc.id IN (:ids)
            AND usc.active = true
            """)
    long inactivateSubcategories(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
