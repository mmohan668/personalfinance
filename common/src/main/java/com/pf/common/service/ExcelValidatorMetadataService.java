package com.pf.common.service;

import com.pf.common.constants.CommonConstants;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.entity.userManagement.User;
import com.pf.common.entity.validator.ExcelValidatorMetadata;
import com.pf.common.enums.AppEnums;
import com.pf.common.record.generic.ExcelValidationResult;
import com.pf.common.repository.setting.SystemConfigRepository;
import com.pf.common.repository.validator.ExcelValidatorMetadataRepository;
import com.pf.common.util.DateUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.pf.common.constants.CommonConstants.COMMA;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelValidatorMetadataService {
    private final ExcelValidatorMetadataRepository excelValidatorMetadataRepository;
    private final EntityManager entityManager;
    private final SystemConfigRepository systemConfigRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional(readOnly = true)
    public ExcelValidationResult validateExcel(String uploadType, Workbook workbook, User user) {
        List<ExcelValidatorMetadata> excelValidatorMetadataList =
                excelValidatorMetadataRepository.findByUploadType(uploadType);
        if (excelValidatorMetadataList.isEmpty()) {
            log.error("No excel validator metadata found for upload type {}", uploadType);
            throw new IllegalArgumentException(
                    "No validator metadata found for upload type: " + uploadType
            );
        }
        String sheetName = excelValidatorMetadataList.getFirst().getSheetName();
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            log.error("No sheet found for sheet name {}", sheetName);
            throw new IllegalArgumentException(
                    "Sheet not found: " + sheetName
            );
        }
        int nonEmptyRowsCount = 0;
        String dateFormat = getUserDateFormat(user);
        Map<Integer, List<String>> errors = new HashMap<>();
        for (Row row : sheet) {
            // Skip header
            if (row.getRowNum() == 0) {
                continue;
            }
            // Stop processing when an entirely blank row is encountered
            if (isBlankRow(row)) {
                break;
            }
            nonEmptyRowsCount++;
            validateRow(row, excelValidatorMetadataList, errors, dateFormat);
        }
        return new ExcelValidationResult(errors, nonEmptyRowsCount);
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK &&
                    !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getUserDateFormat(User user) {
        String dateFormat = CommonConstants.DD_MM_YYYY;
        SystemConfig systemConfig = systemConfigRepository.findByConfigNameAndUserId(
                AppEnums.SystemConfigName.CURRENCY.getValue(),
                user.getId()
        );
        if (systemConfig != null && systemConfig.getConfigValue() != null) {
            if (systemConfig.getConfigValue().getReferenceCode3() != null) {
                dateFormat = systemConfig.getConfigValue().getReferenceCode3();
            }
        }
        return dateFormat;
    }

    private void validateRow(Row row, List<ExcelValidatorMetadata> excelValidatorMetadataList, Map<Integer, List<String>> errors, String dateFormat) {
        for (ExcelValidatorMetadata metadata : excelValidatorMetadataList) {
            Cell cell = row.getCell(metadata.getColumnIndex());
            String cellValue = cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
            boolean isBlank = cellValue.isEmpty();
            //Required validation
            if (metadata.getRequired() && isBlank) {
                addError(row, errors, metadata.getColumnName() + " should not be blank");
                continue;
            }
            // Optional blank field - skip remaining validations
            if (isBlank) {
                continue;
            }
            if (!validateLength(row, errors, metadata, cellValue)) {
                continue;
            }
            //Datatype validation
            if (!validateDataType(row, errors, dateFormat, metadata, cellValue)) {
                continue;
            }
            //DB Validation
            validateDatabase(row, excelValidatorMetadataList, errors, metadata, cellValue);
        }
    }

    private void validateDatabase(Row row, List<ExcelValidatorMetadata> excelValidatorMetadataList, Map<Integer, List<String>> errors, ExcelValidatorMetadata metadata, String cellValue) {
        if (metadata.getDbValidationType() == null) {
            return;
        }
        String[] params = getParams(metadata, excelValidatorMetadataList, row);
        AppEnums.DbValidationType dbValidationType;
        try {
            dbValidationType = AppEnums.DbValidationType.valueOf(metadata.getDbValidationType());
        } catch (IllegalArgumentException e) {
            log.error("Invalid db validation type {}", metadata.getDbValidationType());
            throw new IllegalArgumentException("Invalid db validation: " + metadata.getDbValidationType());
        }
        boolean dbValidationResult = executeDBValidation(metadata.getDbValidationQuery(), params);
        if (dbValidationType == AppEnums.DbValidationType.DB_EXISTS && !dbValidationResult) {
            addError(row, errors, metadata.getColumnName() + " has invalid value: " + cellValue);
        } else if (dbValidationType == AppEnums.DbValidationType.DB_DUPLICATE && dbValidationResult) {
            addError(row, errors, metadata.getColumnName() + " already exists: " + cellValue);
        }
    }

    private boolean validateDataType(Row row, Map<Integer, List<String>> errors, String dateFormat, ExcelValidatorMetadata metadata, String cellValue) {
        if (metadata.getDataType() != null) {
            AppEnums.DataType dataType = AppEnums.DataType.valueOf(metadata.getDataType());
            if (dataType == AppEnums.DataType.NUMBER) {
                try {
                    Double.parseDouble(cellValue);
                } catch (NumberFormatException e) {
                    addError(row, errors, metadata.getColumnName() + " should be a number");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.P_NUMBER) {
                try {
                    double value = Double.parseDouble(cellValue);
                    if (value < 0) {
                        addError(row, errors, metadata.getColumnName() + " should be zero or a positive number");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    addError(row, errors, metadata.getColumnName() + " should be zero or a positive number");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.AMOUNT) {
                try {
                    new BigDecimal(cellValue);
                } catch (NumberFormatException e) {
                    addError(row, errors, metadata.getColumnName() + " should be a valid amount");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.DATE) {
                LocalDate date = DateUtils.toLocalDate(cellValue, dateFormat);
                if (date == null) {
                    addError(row, errors, metadata.getColumnName() + " should be in " + dateFormat + " format");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateLength(Row row, Map<Integer, List<String>> errors, ExcelValidatorMetadata metadata, String cellValue) {
        //Min Length
        if (metadata.getMinLength() != null && cellValue.length() < metadata.getMinLength()) {
            addError(row, errors, metadata.getColumnName() + " should be at least " + metadata.getMinLength() + " characters");
            return false;
        }
        //Max Length
        if (metadata.getMaxLength() != null && cellValue.length() > metadata.getMaxLength()) {
            addError(row, errors, metadata.getColumnName() + " should not exceed " + metadata.getMaxLength() + " characters");
            return false;
        }
        return true;
    }

    private boolean executeDBValidation(String query, String... queryParams) {
        if (query == null || query.isBlank()) {
            log.error("Empty query parameter");
            throw new IllegalArgumentException("DB validation query is missing");
        }
        Query queryObject = entityManager.createNativeQuery(query);
        for (int i = 0; i < queryParams.length; i++) {
            queryObject.setParameter(i + 1, queryParams[i].toLowerCase(Locale.ROOT));
        }
        Object result = queryObject.getSingleResult();
        return Boolean.TRUE.equals(result);
    }

    private String[] getParams
            (
                    ExcelValidatorMetadata excelValidatorMetadata,
                    List<ExcelValidatorMetadata> excelValidatorMetadataList,
                    Row row
            ) {
        String[] paramsColumnNames = excelValidatorMetadata.getDbValidationColumns().split(COMMA);
        List<String> paramsColumnValues = new ArrayList<>();
        for (String columnName : paramsColumnNames) {
            excelValidatorMetadataList.stream()
                    .filter(metadata -> metadata.getColumnName().equals(columnName.trim()))
                    .map(ExcelValidatorMetadata::getColumnIndex)
                    .findFirst()
                    .ifPresent(columnIndex -> {
                        Cell cell = row.getCell(columnIndex);
                        paramsColumnValues.add(cell == null ? "" : dataFormatter.formatCellValue(cell).trim());
                    });
        }
        return paramsColumnValues.toArray(new String[0]);
    }

    private void addError(
            Row row,
            Map<Integer, List<String>> errors,
            String message) {

        errors.computeIfAbsent(row.getRowNum(), k -> new ArrayList<>())
                .add(message);
    }
}
