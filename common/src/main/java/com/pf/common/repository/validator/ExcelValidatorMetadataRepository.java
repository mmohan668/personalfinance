package com.pf.common.repository.validator;

import com.pf.common.entity.validator.ExcelValidatorMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ExcelValidatorMetadataRepository extends JpaRepository<ExcelValidatorMetadata, Integer> {

    @Transactional(readOnly = true)
    List<ExcelValidatorMetadata> findByUploadType(String uploadType);
}
