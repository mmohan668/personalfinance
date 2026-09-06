package com.pf.common.repository;

import com.pf.common.entity.gp.GridColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GridColumnRepository extends JpaRepository<GridColumn, Long> {
    List<GridColumn> findByGridName_NameOrderByVisibleIndex(String gridName);
}
