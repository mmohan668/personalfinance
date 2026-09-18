package com.pf.common.repository.setting;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.entity.settings.ReferenceValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
            SELECT new com.pf.common.dto.generic.SelectItem(
                        rv.id,
                        rv.referenceCode
            )
            FROM ReferenceValue rv
            WHERE rv.referenceObject.refObjName = 'CATEGORY_TYPE'
            """)
    List<SelectItem> fetchCategoryTypes();
}
