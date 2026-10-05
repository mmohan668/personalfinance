package com.pf.common.repository.setting;

import com.pf.common.record.generic.SelectItem;
import com.pf.common.entity.settings.ReferenceValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReferenceValueRepository extends JpaRepository<ReferenceValue, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT count(rv) FROM ReferenceValue rv
            WHERE rv.referenceObject.id = :refObjId
            AND LOWER(TRIM(referenceCode)) = LOWER(TRIM(:refCode))
            """)
    long countByReferenceObjectIdAndReferenceCode(
            @Param("refObjId") Long refObjId,
            @Param("refCode") String refCode
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(rv)
            FROM ReferenceValue rv
            WHERE rv.referenceObject.id = :refObjId
            AND LOWER(TRIM(rv.referenceCode)) = LOWER(TRIM(:referenceCode)) AND rv.id <> :id
            """)
    long countByReferenceObjectIdAndReferenceCodeAndIdNot(
            @Param("refObjId") Long refObjId,
            @Param("referenceCode") String referenceCode,
            @Param("id") Long id
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.record.generic.SelectItem(
                        rv.id,
                        rv.referenceCode || ' ~ '|| rv.referenceCodeDescription
            )
            FROM ReferenceValue rv
            WHERE rv.referenceObject.refObjName = :refObjName
            ORDER BY LOWER(rv.referenceCode)
            """)
    List<SelectItem> fetchReferenceValuesByRefObjName(@Param("refObjName") String refObjName);

    @Transactional(readOnly = true)
    @Query("""
            SELECT rv.id
            FROM ReferenceValue rv
            WHERE rv.referenceCode = :referenceCode
            AND rv.referenceObject.refObjName = :refObjName
            """)
    Long fetchIdByReferenceCodeAndRefObjName(
            @Param("referenceCode") String referenceCode,
            @Param("refObjName") String refObjName
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT rv.referenceCode
            FROM ReferenceValue rv
            WHERE rv.referenceObject.refObjName = :refObjName
            ORDER BY LOWER(rv.referenceCode)
            """)
    List<String> findReferenceCodeByRefObjName(@Param("refObjName") String refObjName);

    @Transactional(readOnly = true)
    @Query("""
                SELECT COUNT(rv) > 0 FROM ReferenceValue rv
                WHERE rv.referenceObject.id IN (:refObjIds)
            """)
    boolean existsByReferenceObject(@Param("refObjIds") List<Long> refObjIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ReferenceValue rv
            SET rv.active = true, rv.updatedBy.id = :updatedBy, rv.updatedAt = :updatedAt
            WHERE rv.active = false
            AND rv.id IN (:ids)
            """)
    int activateReferenceValues(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ReferenceValue rv
            SET rv.active = false, rv.updatedBy.id = :updatedBy, rv.updatedAt = :updatedAt
            WHERE rv.active = true
            AND rv.id IN (:ids)
            """)
    int inactivateReferenceValues(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
