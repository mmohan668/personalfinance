package com.pf.common.repository.setting;

import com.pf.common.entity.settings.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    @Transactional(readOnly = true)
    @Query("""
                SELECT COUNT(sc) > 0
                FROM SystemConfig sc
                WHERE sc.configValue.id IN :ids
            """)
    boolean existsByConfigValueIdIn(@Param("ids") List<Long> ids);
}
