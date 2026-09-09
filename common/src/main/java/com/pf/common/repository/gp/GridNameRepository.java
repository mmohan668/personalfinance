package com.pf.common.repository.gp;

import com.pf.common.entity.gp.GridName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GridNameRepository extends JpaRepository<GridName, Long> {
    @Transactional(readOnly = true)
    @Query("SELECT g.id FROM GridName g WHERE g.name=:name")
    Long findIdByName(@Param("name") String name);
}
