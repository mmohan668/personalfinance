package com.pf.common.service.bulkUpload;

import com.pf.common.dto.generic.TemplateRowData;
import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import com.pf.common.entity.generic.TemplateHeader;
import com.pf.common.entity.generic.TemplateName;
import com.pf.common.entity.userManagement.User;
import com.pf.common.enums.AppEnums;
import com.pf.common.enums.BulkUploadStatus;
import com.pf.common.properties.BulkUploadProperties;
import com.pf.common.record.generic.ExcelValidationResult;
import com.pf.common.repository.bulkUpload.BulkUploadsStatusRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.ExcelValidationService;
import com.pf.common.service.settings.SettingsService;
import com.pf.common.util.DateUtils;
import com.pf.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadProcessingService {
    private final UserRepository userRepository;
    private final BulkUploadsStatusRepository bulkUploadsStatusRepository;
    private final ExcelValidationService excelValidationService;
    private final BulkUploadProperties bulkUploadProperties;
    private final SettingsService settingsService;

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
        try (FileInputStream fis = new FileInputStream(filePath); XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis)) {
            ExcelValidationResult validationResult = excelValidationService.validateExcel(bulkUploadsStatus.getTemplateName().getTemplateName(), workbook);
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
                    processValidRows(workbook, bulkUploadsStatus, validationResult.errors().keySet());
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

    public void processValidRows(XSSFWorkbook workbook, BulkUploadsStatus bulkUploadsStatus, Set<Integer> validRows) {
        String templateName = bulkUploadsStatus.getTemplateName().getTemplateName();
        AppEnums.TemplateName template = AppEnums.TemplateName.valueOf(templateName);
        switch (template) {
            case REFERENCE_VALUES: {
                Set<TemplateRowData> templateRowData = buildValidRows(workbook, bulkUploadsStatus.getTemplateName(), validRows);
                settingsService.saveReferenceValuesBulk(templateRowData, bulkUploadsStatus.getUser());
            }
            case CATEGORIES:
            case SUBCATEGORIES:
            case FINANCIAL_TRANSACTIONS:
        }
    }

    public Set<TemplateRowData> buildValidRows(XSSFWorkbook workbook, TemplateName templateName, Set<Integer> invalidRows) {
        XSSFSheet sheet = workbook.getSheet(templateName.getSheetName());
        Set<TemplateHeader> templateHeaders = templateName.getTemplateHeaders();
        Set<TemplateRowData> templateRowDataList = new HashSet<>();
        Map<Integer, TemplateHeader> headersByColumn = templateHeaders.stream().collect(Collectors.toMap(TemplateHeader::getColumnIndex, Function.identity()));
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            if (invalidRows.contains(i)) {
                continue;
            }
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            TemplateRowData rowData = new TemplateRowData();
            for (Map.Entry<Integer, TemplateHeader> entry : headersByColumn.entrySet()) {
                Integer columnIndex = entry.getKey();
                TemplateHeader header = entry.getValue();
                Cell cell = row.getCell(columnIndex);
                Object value = getCellValue(cell, AppEnums.DataType.valueOf(header.getDataType()));
                rowData.put(header.getDbColumnName(), value);
            }
            templateRowDataList.add(rowData);
        }
        return templateRowDataList;
    }

    private Object getCellValue(
            Cell cell,
            AppEnums.DataType dataType) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        return switch (dataType) {
            case TEXT -> getTextCellValue(cell);
            case NUMBER, P_NUMBER, AMOUNT -> {
                if (cell.getCellType() != CellType.NUMERIC) {
                    throw new IllegalArgumentException(
                            "Expected numeric value but found: "
                                    + cell.getCellType()
                    );
                }
                yield BigDecimal
                        .valueOf(cell.getNumericCellValue())
                        .stripTrailingZeros()
                        .toPlainString();
            }
            case DATE -> getDateCellValue(cell);
        };
    }

    private String getTextCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> BigDecimal
                    .valueOf(cell.getNumericCellValue())
                    .stripTrailingZeros()
                    .toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> null;
            default -> cell.toString().trim();
        };
    }

    private LocalDate getDateCellValue(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) {
                return null;
            }
            return DateUtils.toLocalDate(value);
        }
        throw new IllegalArgumentException(
                "Invalid date cell type: " + cell.getCellType()
        );
    }

    private String createErrorFile(String uploadedFilePath, String uploadedType, Map<Integer, List<String>> errors) throws IOException {
        Path inputPath = Paths.get(uploadedFilePath);
        String fileName = inputPath.getFileName().toString();
        String errorFileName = "Error_" + fileName;
        Path errorFilePath = FileUtils.getDateTimePath(bulkUploadProperties.getError(), uploadedType).resolve(errorFileName);
        try (FileInputStream fis = new FileInputStream(uploadedFilePath); Workbook workbook = WorkbookFactory.create(fis); FileOutputStream fos = new FileOutputStream(errorFilePath.toFile())) {
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
                remarksCell.setCellValue(String.join("\n", rowErrors));
                remarksCell.setCellStyle(errorStyle);
                // Increase row height so all error lines are visible
                row.setHeightInPoints(rowErrors.size() * 15);
            }
            workbook.write(fos);
        }
        return errorFilePath.toString();
    }
}
