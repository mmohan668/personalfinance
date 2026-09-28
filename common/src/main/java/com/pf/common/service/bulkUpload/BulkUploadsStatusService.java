package com.pf.common.service.bulkUpload;

import com.pf.common.dto.bulkUpload.BulkUploadsStatusDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import com.pf.common.mapper.bulkUpload.BulkUploadsStatusMapper;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadsStatusService extends BaseService {
    private final BulkUploadsStatusMapper bulkUploadsStatusMapper;

    public GridResult fetchBulkUploadStatusGridData(SearchCriteria searchCriteria) {
        log.debug("fetchBulkUploadStatusGridData: {}", searchCriteria);
        searchCriteria.setFIELD_MAPPINGS(BulkUploadsStatusDto.FIELD_MAPPINGS);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(BulkUploadsStatus.class, searchCriteria);
        }
        List<BulkUploadsStatusDto> recordDetails = bulkUploadsStatusMapper.toDtoList(getDataBySearchCriteria(BulkUploadsStatus.class, searchCriteria));
        log.debug("fetchBulkUploadStatusGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }
}
