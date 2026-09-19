package com.pf.common.repository.gp;

import com.pf.common.entity.gp.GridColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GridColumnRepository extends JpaRepository<GridColumn, Long> {
    @Transactional(readOnly = true)
    List<GridColumn> findByGridName_NameOrderByVisibleIndex(String gridName);
}
