package com.pf.common.repository.setting;

import com.pf.common.record.generic.SelectItem;
import com.pf.common.entity.settings.ReferenceObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReferenceObjectRepository extends JpaRepository<ReferenceObject, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.record.generic.SelectItem(
                        ro.id,
                        ro.refObjName
            )
            FROM ReferenceObject ro
            """)
    List<SelectItem> fetchRefObjNames();

    @Transactional(readOnly = true)
    @Query("""
            SELECT ro.refObjName
            FROM ReferenceObject ro
            ORDER BY LOWER(ro.refObjName) ASC
            """)
    List<String> findRefObjNamesOrderByRefObjNameAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ReferenceObject ro
            SET ro.active = false, ro.updatedBy.id = :modifiedBy, ro.updatedAt = :modifiedAt
            WHERE ro.active = true
            AND ro.id IN (:refObjIds)
            """)
    int inactivateReferenceObject(
            @Param("refObjIds") List<Long> refObjIds,
            @Param("modifiedBy") Long modifiedBy,
            @Param("modifiedAt") LocalDateTime modifiedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ReferenceObject ro
            SET ro.active = true, ro.updatedBy.id = :modifiedBy, ro.updatedAt = :modifiedAt
            WHERE ro.active = false
            AND ro.id IN (:refObjIds)
            """)
    int activateReferenceObject(
            @Param("refObjIds") List<Long> refObjIds,
            @Param("modifiedBy") Long modifiedBy,
            @Param("modifiedAt") LocalDateTime modifiedAt
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(ro) > 0 FROM ReferenceObject ro
            WHERE LOWER(TRIM(ro.refObjName)) = LOWER(TRIM(:refObjName))
            """)
    boolean existsByRefObjName(@Param("refObjName") String refObjName);

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(ro) > 0 FROM ReferenceObject ro
            WHERE LOWER(TRIM(ro.refObjName)) = LOWER(TRIM(:refObjName))
            AND ro.id <> :refObjId
            """)
    boolean existsByRefObjNameAndId(
            @Param("refObjName") String refObjName,
            @Param("refObjId") Long refObjId
    );
}
