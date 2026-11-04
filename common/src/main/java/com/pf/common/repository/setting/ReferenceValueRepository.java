package com.pf.common.repository.setting;

import com.pf.common.entity.settings.UserReferenceValue;
import com.pf.common.record.generic.SelectItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReferenceValueRepository extends JpaRepository<UserReferenceValue, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(urv) > 0
            FROM UserReferenceValue urv
            WHERE urv.referenceObject.id = :refObjId
            AND LOWER(TRIM(urv.referenceCode)) = LOWER(TRIM(:refCode))
            """)
    boolean existsByReferenceObjectIdAndReferenceCode(
            @Param("refObjId") Long refObjId,
            @Param("refCode") String refCode
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(urv) > 0
            FROM UserReferenceValue urv
            WHERE urv.referenceObject.id = :refObjId
            AND LOWER(TRIM(urv.referenceCode)) = LOWER(TRIM(:referenceCode))
            AND urv.id <> :id
            AND urv.user.id = :userId
            """)
    boolean existsByReferenceObjectIdAndReferenceCodeAndIdNot(
            @Param("refObjId") Long refObjId,
            @Param("referenceCode") String referenceCode,
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.record.generic.SelectItem(
                        urv.id,
                        urv.referenceCode || ' ~ ' || urv.referenceCodeDescription
            )
            FROM UserReferenceValue urv
            WHERE urv.referenceObject.refObjName = :refObjName
            AND urv.user.id = :userId
            AND urv.active = true
            ORDER BY LOWER(urv.referenceCode)
            """)
    List<SelectItem> fetchReferenceValuesByRefObjName(
            @Param("refObjName") String refObjName,
            @Param("userId") Long userId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT urv.id
            FROM UserReferenceValue urv
            WHERE urv.referenceCode = :referenceCode
            AND urv.referenceObject.refObjName = :refObjName
            AND urv.user.id = :userId
            AND urv.active = true
            """)
    Long fetchIdByReferenceCodeAndRefObjName(
            @Param("referenceCode") String referenceCode,
            @Param("refObjName") String refObjName,
            @Param("userId") Long userId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT urv.referenceCode
            FROM UserReferenceValue urv
            WHERE urv.referenceObject.refObjName = :refObjName
            ORDER BY LOWER(urv.referenceCode)
            """)
    List<String> findReferenceCodeByRefObjName(@Param("refObjName") String refObjName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserReferenceValue urv
            SET urv.active = true,
                urv.updatedBy.id = :updatedBy,
                urv.updatedAt = :updatedAt
            WHERE urv.active = false
            AND urv.id IN (:ids)
            """)
    int activateReferenceValues(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserReferenceValue urv
            SET urv.active = false,
                urv.updatedBy.id = :updatedBy,
                urv.updatedAt = :updatedAt
            WHERE urv.active = true
            AND urv.id IN (:ids)
            """)
    int inactivateReferenceValues(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}