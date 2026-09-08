package com.pf.common.repository;

import com.pf.common.entity.gp.GridPersonalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GridPersonalizationRepository extends JpaRepository<GridPersonalization, Long> {

    Optional<GridPersonalization> findByGridName_IdAndUserId(Long id, Long userId);
}
