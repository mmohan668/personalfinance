package com.pf.common.repository.categoryManagement;

import com.pf.common.entity.categoryManagement.UserSubcategory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSubcategoryRepository extends CrudRepository<UserSubcategory, Long> {

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
}
