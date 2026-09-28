package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.service.bulkUpload.BulkUploadsStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bulkupload")
@RequiredArgsConstructor
public class BulkUploadsStatusController {
    private final BulkUploadsStatusService bulkUploadsStatusService;

    @PostMapping("/fetchBulkUploadStatusGridData")
    public GridResult fetchBulkUploadStatusGridData(@RequestBody SearchCriteria searchCriteria) {
        return bulkUploadsStatusService.fetchBulkUploadStatusGridData(searchCriteria);
    }
}
