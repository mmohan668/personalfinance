package com.pf.common.service.bulkUpload;

import com.pf.common.dto.bulkUpload.BulkUploadsStatusDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.entity.generic.TemplateName;
import com.pf.common.mapper.bulkUpload.BulkUploadsStatusMapper;
import com.pf.common.properties.BulkUploadProperties;
import com.pf.common.record.bulkUpload.BulkUploadCreatedEvent;
import com.pf.common.repository.bulkUpload.BulkUploadsStatusRepository;
import com.pf.common.repository.generic.TemplateNameRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.pf.common.enums.BulkUploadStatus.RECEIVED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadsService extends BaseService {
    private final BulkUploadsStatusMapper bulkUploadsStatusMapper;
    private final BulkUploadProperties bulkUploadProperties;
    private final BulkUploadsStatusRepository bulkUploadsStatusRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TemplateNameRepository templateNameRepository;

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

    @Transactional
    public ApiResponse uploadBulkUploadTemplate(String uploadType, String uploadTypeLabel, MultipartFile file) {
        log.info("Uploading Bulk Upload Template, Upload Type: {}", uploadType);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Uploaded file does not have a valid filename");
        }
        try {
            Path uploadDirectory = FileUtils.getDateTimePath(bulkUploadProperties.getInProgress(), uploadType);
            String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            Path targetFile = uploadDirectory.resolve(fileName);
            file.transferTo(targetFile);
            BulkUploadsStatus bulkUploadsStatus =
                    bulkUploadsStatusRepository.save(createBulkUploadsStatus(uploadTypeLabel, uploadType, targetFile));
            log.info("Sending Bulk Upload Status to Bulk Upload Processing Service");
            applicationEventPublisher.publishEvent(new BulkUploadCreatedEvent(
                    bulkUploadsStatus.getId(),
                    fetchLoginUser().getId()
            ));
        } catch (IOException e) {
            log.error("Error while uploading file", e);
            throw new RuntimeException("Failed to save uploaded file", e);
        }
        return success(uploadTypeLabel + " Uploaded successfully");
    }

    public ResponseEntity<Resource> downloadFile(String filePath) throws IOException {
        log.debug("Request for downloadFile: {}", filePath);
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(path.toUri());
        String fileName = path.getFileName().toString();
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private @NonNull BulkUploadsStatus createBulkUploadsStatus(String uploadTypeLabel, String uploadType, Path targetFile) {
        TemplateName templateName = templateNameRepository.findByTemplateName(uploadType);
        BulkUploadsStatus bulkUploadsStatus = new BulkUploadsStatus();
        bulkUploadsStatus.setUploadType(uploadTypeLabel);
        bulkUploadsStatus.setUploadedFile(targetFile.toString());
        bulkUploadsStatus.setStatus(RECEIVED.getValue());
        bulkUploadsStatus.setUser(fetchLoginUser().getAdminUser());
        bulkUploadsStatus.setRemarks(RECEIVED.getDescription());
        bulkUploadsStatus.setCreatedBy(fetchLoginUser());
        bulkUploadsStatus.setTemplateName(templateName);
        return bulkUploadsStatus;
    }
}
