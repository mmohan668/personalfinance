package com.pf.common.repository.generic;

import com.pf.common.entity.generic.TemplateName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TemplateNameRepository extends JpaRepository<TemplateName, String> {

    @Transactional(readOnly = true)
    TemplateName findByTemplateName(String templateName);
}
