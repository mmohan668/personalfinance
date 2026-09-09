package com.pf.common.repository.setting;

import com.pf.common.entity.settings.ReferenceObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceObjectRepository extends JpaRepository<ReferenceObject, Long> {
}
