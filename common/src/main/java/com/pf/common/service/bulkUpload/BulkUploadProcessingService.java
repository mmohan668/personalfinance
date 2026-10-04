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
import com.pf.common.util.DateUtils;
import com.pf.common.util.ExcelUtils;
import com.pf.common.util.FileUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

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
                    processValidRows(workbook, bulkUploadsStatus, validationResult.errors().keySet());
                }
            } else {
                log.info("Excel validated successfully");
                bulkUploadsStatus.setStatus(BulkUploadStatus.SUCCESS.getValue());
                bulkUploadsStatus.setRemarks(BulkUploadStatus.SUCCESS.getDescription());
                bulkUploadsStatusRepository.saveAndFlush(bulkUploadsStatus);
                processValidRows(workbook, bulkUploadsStatus, validationResult.errors().keySet());
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
        Set<TemplateRowData> templateRowData = buildValidRows(workbook, bulkUploadsStatus.getTemplateName(), validRows);
        log.info("Saving reference values bulk thru bulk upload template");
        if (templateRowData == null || templateRowData.isEmpty()) {
            log.warn("saveReferenceValuesBulk: template row data is empty.");
            return;
        }
        String json = objectMapper.writeValueAsString(templateRowData.stream().map(TemplateRowData::getValues).toList());
        switch (template) {
            case REFERENCE_VALUES: {
                saveReferenceValuesBulk(json, bulkUploadsStatus.getUser());
            }
            case CATEGORIES: {
                saveCategoriesBulk(json, bulkUploadsStatus.getUser());
            }
            case SUBCATEGORIES: {
                saveSubcategoriesBulk(json, bulkUploadsStatus.getUser());
            }
            case FINANCIAL_TRANSACTIONS: {
                saveFinancialTransactionsBulk(json, bulkUploadsStatus.getUser());
            }
        }
    }

    public Set<TemplateRowData> buildValidRows(XSSFWorkbook workbook, TemplateName templateName, Set<Integer> invalidRows) {
        XSSFSheet sheet = workbook.getSheet(templateName.getSheetName());
        Set<TemplateHeader> templateHeaders = templateName.getTemplateHeaders();
        Set<TemplateRowData> templateRowDataList = new HashSet<>();
        Map<Integer, TemplateHeader> headersByColumn = templateHeaders.stream().collect(Collectors.toMap(TemplateHeader::getColumnIndex, Function.identity()));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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
        String errorFileName = "ERROR_" + fileName;
        Path errorFilePath = FileUtils.getDateTimePath(bulkUploadProperties.getError(), uploadedType).resolve(errorFileName);
        try (FileInputStream fis = new FileInputStream(uploadedFilePath); XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis); FileOutputStream fos = new FileOutputStream(errorFilePath.toFile())) {
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
            CellStyle headerStyle = ExcelUtils.createHeaderStyle(workbook, true);
            Cell remarksHeader = headerRow.createCell(remarksColumnIndex);
            remarksHeader.setCellValue("Error Messages");
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
            /*
             * Remove rows which don't have errors.
             *
             * Delete from bottom to top so that row indexes
             * in the errors map remain valid while deleting.
             */
            for (int rowNumber = sheet.getLastRowNum(); rowNumber >= 1; rowNumber--) {
                if (!errors.containsKey(rowNumber)) {
                    Row row = sheet.getRow(rowNumber);
                    if (row != null) {
                        sheet.removeRow(row);
                    }
                    if (rowNumber < sheet.getLastRowNum()) {
                        sheet.shiftRows(
                                rowNumber + 1,
                                sheet.getLastRowNum(),
                                -1
                        );
                    }
                }
            }
            workbook.write(fos);
        }
        return errorFilePath.toString();
    }

    @Transactional
    public void saveReferenceValuesBulk(String json, User user) {
        entityManager.createNativeQuery("""
                CALL insert_reference_values_bulk(
                    :userId,
                    :createdBy,
                    CAST(:rows AS jsonb)
                )
                """).setParameter("userId", user.getAdminUser().getId()).setParameter("createdBy", user.getId()).setParameter("rows", json).executeUpdate();
        log.info("save reference values bulk finished successfully.");
    }

    @Transactional
    public void saveCategoriesBulk(String json, User user) {
        entityManager.createNativeQuery("""
                CALL insert_user_categories_bulk(
                    :userId,
                    :createdBy,
                    CAST(:rows AS jsonb)
                )
                """).setParameter("userId", user.getAdminUser().getId()).setParameter("createdBy", user.getId()).setParameter("rows", json).executeUpdate();
        log.info("save user categories bulk finished successfully.");
    }

    @Transactional
    public void saveSubcategoriesBulk(String json, User user) {
        entityManager.createNativeQuery("""
                CALL insert_user_subcategories_bulk(
                    :userId,
                    :createdBy,
                    CAST(:rows AS jsonb)
                )
                """).setParameter("userId", user.getAdminUser().getId()).setParameter("createdBy", user.getId()).setParameter("rows", json).executeUpdate();
        log.info("save user subcategories bulk finished successfully.");
    }

    @Transactional
    public void saveFinancialTransactionsBulk(String json, User user) {
        entityManager.createNativeQuery("""
                CALL insert_financial_transactions_bulk(
                    :userId,
                    :createdBy,
                    CAST(:rows AS jsonb)
                )
                """).setParameter("userId", user.getAdminUser().getId()).setParameter("createdBy", user.getId()).setParameter("rows", json).executeUpdate();
        log.info("save financial transactions bulk finished successfully.");
    }
}
