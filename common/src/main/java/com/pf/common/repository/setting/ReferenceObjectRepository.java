package com.pf.common.repository.setting;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.entity.settings.ReferenceObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReferenceObjectRepository extends JpaRepository<ReferenceObject, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT new com.pf.common.dto.generic.SelectItem(
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

}
