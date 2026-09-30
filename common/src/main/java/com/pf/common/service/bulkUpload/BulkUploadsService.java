package com.pf.common.service.bulkUpload;

import com.pf.common.dto.bulkUpload.BulkUploadsStatusDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.mapper.bulkUpload.BulkUploadsStatusMapper;
import com.pf.common.properties.BulkUploadProperties;
import com.pf.common.repository.bulkUpload.BulkUploadsStatusRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadsService extends BaseService {
    private final BulkUploadsStatusMapper bulkUploadsStatusMapper;
    private final BulkUploadProperties bulkUploadProperties;
    private final BulkUploadsStatusRepository bulkUploadsStatusRepository;

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
        System.out.println("Upload type: " + uploadType);
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());
        String dateTimeFolder = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        Path uploadDirectory = Paths.get(
                bulkUploadProperties.getInProgress(),
                uploadType,
                dateTimeFolder
        );
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Uploaded file does not have a valid filename");
        }
        try {
            Files.createDirectories(uploadDirectory);
            String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            Path targetFile = uploadDirectory.resolve(fileName);
            file.transferTo(targetFile);
            System.out.println(targetFile);
            BulkUploadsStatus bulkUploadsStatus = new BulkUploadsStatus();
            bulkUploadsStatus.setUploadType(uploadTypeLabel);
            bulkUploadsStatus.setUploadedFile(targetFile.toString());
            bulkUploadsStatus.setStatus("In Progress");
            bulkUploadsStatus.setUser(fetchLoginUser().getAdminUser());
            bulkUploadsStatus.setCreatedBy(fetchLoginUser());
            bulkUploadsStatusRepository.save(bulkUploadsStatus);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
        return success(uploadTypeLabel + " Uploaded successfully");
    }

    public ResponseEntity<Resource> downloadFile(String filePath) throws IOException {
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
}
