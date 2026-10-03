package com.pf.common.service.bulkUpload;

import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import com.pf.common.entity.userManagement.User;
import com.pf.common.enums.BulkUploadStatus;
import com.pf.common.properties.BulkUploadProperties;
import com.pf.common.record.generic.ExcelValidationResult;
import com.pf.common.repository.bulkUpload.BulkUploadsStatusRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.ExcelValidationService;
import com.pf.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadProcessingService {
    private final UserRepository userRepository;
    private final BulkUploadsStatusRepository bulkUploadsStatusRepository;
    private final ExcelValidationService excelValidationService;
    private final BulkUploadProperties bulkUploadProperties;

    @Transactional
    public void processBulkUpload(Long id, Long userId) {
        log.info("Processing Bulk Upload Processing for id = {}", id);
        BulkUploadsStatus bulkUploadsStatus = bulkUploadsStatusRepository.findById(id).orElse(null);
        if (bulkUploadsStatus == null) {
            log.error("Bulk Upload Processing Service Error: Bulk Upload Status Not Found");
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found");
            return;
        }
        log.info("Started Bulk Upload Processing Service");
        bulkUploadsStatus.setStatus(BulkUploadStatus.IN_PROCESS.getValue());
        bulkUploadsStatus.setRemarks(BulkUploadStatus.IN_PROCESS.getDescription());
        bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
        String filePath = bulkUploadsStatus.getUploadedFile();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            ExcelValidationResult validationResult =
                    excelValidationService.validateExcel(bulkUploadsStatus.getTemplateName().getTemplateName(), workbook);
            if (validationResult.nonEmptyRowsCount() == 0) {
                log.warn("Excel file contains no data rows for id = {}", id);
                bulkUploadsStatus.setStatus(BulkUploadStatus.NO_DATA.getValue());
                bulkUploadsStatus.setRemarks(BulkUploadStatus.NO_DATA.getDescription());
                bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
                log.info("Bulk Upload contains no data for id = {}", id);
            } else if (!validationResult.errors().isEmpty()) {
                String errorFilePath = createErrorFile(bulkUploadsStatus.getUploadedFile(), bulkUploadsStatus.getTemplateName().getTemplateName(), validationResult.errors());
                bulkUploadsStatus.setErrorFile(errorFilePath);
                if (validationResult.errors().size() == validationResult.nonEmptyRowsCount()) {
                    log.debug("Validation failed for all rows");
                    bulkUploadsStatus.setStatus(BulkUploadStatus.ERRORS.getValue());
                    bulkUploadsStatus.setRemarks(BulkUploadStatus.ERRORS.getDescription());
                    bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
                    log.info("Bulk Upload marked as ERRORS for id = {}", id);
                } else {
                    log.debug("Validation failed for {} rows for id = {}", validationResult.errors().size(), id);
                    bulkUploadsStatus.setStatus(BulkUploadStatus.PARTIAL_SUCCESS.getValue());
                    bulkUploadsStatus.setRemarks(BulkUploadStatus.PARTIAL_SUCCESS.getDescription());
                    bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
                    //TODO
                    //Need to process valid rows
                }
            } else {
                log.info("Excel validated successfully");
                bulkUploadsStatus.setStatus(BulkUploadStatus.SUCCESS.getValue());
                bulkUploadsStatus.setRemarks(BulkUploadStatus.SUCCESS.getDescription());
                bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
                //TODO
                //Need to process all rows
            }
        } catch (Exception e) {
            log.error("Error while processing Bulk Upload Processing for id = {}", id, e);
            bulkUploadsStatus.setStatus(BulkUploadStatus.FAILED.getValue());
            bulkUploadsStatus.setRemarks(BulkUploadStatus.FAILED.getDescription());
            bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
            log.info("Bulk Upload marked as FAILED for id = {}", id);
        }
    }

    private String createErrorFile(String uploadedFilePath, String uploadedType, Map<Integer, List<String>> errors) throws IOException {
        Path inputPath = Paths.get(uploadedFilePath);
        String fileName = inputPath.getFileName().toString();
        String errorFileName = "Error_" + fileName;
        Path errorFilePath =
                FileUtils.getDateTimePath(bulkUploadProperties.getError(), uploadedType).resolve(errorFileName);
        try (FileInputStream fis = new FileInputStream(uploadedFilePath);
             Workbook workbook = WorkbookFactory.create(fis);
             FileOutputStream fos = new FileOutputStream(errorFilePath.toFile())) {
            Sheet sheet = workbook.getSheet(uploadedType);
            if (sheet == null) {
                throw new IOException("Sheet not found: " + uploadedType);
            }
            int remarksColumnIndex;
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("Excel file does not contain a header row");
            }
            remarksColumnIndex = headerRow.getLastCellNum();
            // Add Remarks header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            Cell remarksHeader = headerRow.createCell(remarksColumnIndex);
            remarksHeader.setCellValue("Remarks");
            sheet.setColumnWidth(remarksColumnIndex, 80 * 256);
            remarksHeader.setCellStyle(headerStyle);
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setWrapText(true);
            Font errorFont = workbook.createFont();
            errorFont.setColor(IndexedColors.RED.getIndex());
            errorStyle.setFont(errorFont);
            // Add errors to corresponding rows
            for (Map.Entry<Integer, List<String>> entry : errors.entrySet()) {
                int rowNumber = entry.getKey();
                List<String> rowErrors = entry.getValue();
                Row row = sheet.getRow(rowNumber);
                if (row == null) {
                    continue;
                }
                Cell remarksCell = row.createCell(remarksColumnIndex);
                remarksCell.setCellValue(
                        String.join("\n", rowErrors)
                );
                remarksCell.setCellStyle(errorStyle);
                // Increase row height so all error lines are visible
                row.setHeightInPoints(rowErrors.size() * 15);
            }
            workbook.write(fos);
        }
        return errorFilePath.toString();
    }
}
