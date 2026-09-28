package com.pf.common.repository.bulkUpload;

import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkUploadsStatusRepository extends JpaRepository<BulkUploadsStatus, Long> {
}
