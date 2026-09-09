package com.pf.common.repository.gp;

import com.pf.common.entity.gp.GridPersonalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface GridPersonalizationRepository extends JpaRepository<GridPersonalization, Long> {

    @Transactional(readOnly = true)
    Optional<GridPersonalization> findByGridName_NameAndUserId(String name, Long userId);

    @Transactional
    @Modifying(clearAutomatically = true,  flushAutomatically = true)
    int deleteByGridName_NameAndUserId(String name, Long userId);
}
