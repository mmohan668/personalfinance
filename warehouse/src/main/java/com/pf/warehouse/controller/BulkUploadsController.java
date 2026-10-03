package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.service.bulkUpload.BulkUploadsService;
import com.pf.common.service.generic.TemplateGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/bulkupload")
@RequiredArgsConstructor
public class BulkUploadsController {
    private final BulkUploadsService bulkUploadsService;
    private final TemplateGenerationService templateGenerationService;

    @PostMapping("/fetchBulkUploadStatusGridData")
    public GridResult fetchBulkUploadStatusGridData(@RequestBody SearchCriteria searchCriteria) {
        return bulkUploadsService.fetchBulkUploadStatusGridData(searchCriteria);
    }

    @GetMapping("/downloadTemplate")
    public ResponseEntity<InputStreamResource> downloadBulkUploadTemplate(@RequestParam String templateName) {
        ByteArrayInputStream inputStream = templateGenerationService.generateTemplate(templateName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
        if (inputStream == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bulk upload template not found"
            );
        }
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping(value = "/uploadTemplate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse uploadBulkUploadTemplate(
            @RequestParam("uploadType") String uploadType,
            @RequestParam("uploadTypeLabel") String uploadTypeLabel,
            @RequestParam("file") MultipartFile file
    ) {
        return bulkUploadsService.uploadBulkUploadTemplate(uploadType, uploadTypeLabel, file);
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) throws IOException {
        return bulkUploadsService.downloadFile(filePath);
    }
}
