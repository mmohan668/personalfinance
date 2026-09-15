package com.pf.common.repository.setting;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.entity.settings.ReferenceObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenceObjectRepository extends JpaRepository<ReferenceObject, Long> {

    @Query("""
            SELECT COUNT(ro)
              FROM ReferenceObject ro
             WHERE LOWER(TRIM(ro.refObjName)) = LOWER(:refObjName)
            """)
    long getCountByReferenceObjectName(
            @Param("refObjName") String refObjName
    );

    @Query("""
            SELECT COUNT(ro)
              FROM ReferenceObject ro
             WHERE LOWER(TRIM(ro.refObjName)) = LOWER(:refObjName) AND ro.id <> :id
            """)
    long getCountByReferenceObjectNameIdNotEquals(
            @Param("refObjName") String refObjName,
            @Param("id") Long id
    );

    @Query("""
            SELECT new com.pf.common.dto.generic.SelectItem(
                        rv.id,
                        rv.refObjName
            )
            FROM ReferenceObject rv
            """)
    List<SelectItem> fetchCategoryTypes();
}
