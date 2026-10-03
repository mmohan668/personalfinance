package com.pf.common.service.generic;

import com.pf.common.entity.generic.TemplateHeader;
import com.pf.common.entity.generic.TemplateName;
import com.pf.common.enums.AppEnums;
import com.pf.common.record.generic.ExcelValidationResult;
import com.pf.common.repository.generic.TemplateNameRepository;
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
public class ExcelValidationService {
    private final EntityManager entityManager;
    private final TemplateNameRepository templateNamesRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional(readOnly = true)
    public ExcelValidationResult validateExcel(String uploadType, Workbook workbook) {
        TemplateName templateNames = templateNamesRepository.findByTemplateName(uploadType);
        if (templateNames == null || templateNames.getTemplateHeaders() == null || templateNames.getTemplateHeaders().isEmpty()) {
            log.error("No template header found for uploadType = {}", uploadType);
            throw new IllegalArgumentException(
                    "No template header found for upload Type: " + uploadType
            );
        }
        String sheetName = templateNames.getSheetName();
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            log.error("No sheet found for sheet name {}", sheetName);
            throw new IllegalArgumentException(
                    "Sheet not found: " + sheetName
            );
        }
        int nonEmptyRowsCount = 0;
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
            validateRow(row, templateNames.getTemplateHeaders(), errors);
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

    private void validateRow(Row row, Set<TemplateHeader> templateHeaders, Map<Integer, List<String>> errors) {
        for (TemplateHeader templateHeader : templateHeaders.stream().sorted(Comparator.comparing(TemplateHeader::getColumnIndex)).toList()) {
            Cell cell = row.getCell(templateHeader.getColumnIndex());
            String cellValue = cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
            boolean isBlank = cellValue.isEmpty();
            //Required validation
            if (templateHeader.isRequired() && isBlank) {
                addError(row, errors, templateHeader.getHeaderName() + " should not be blank");
                continue;
            }
            // Optional blank field - skip remaining validations
            if (isBlank) {
                continue;
            }
            if (!validateLength(row, errors, templateHeader, cellValue)) {
                continue;
            }
            //Datatype validation
            if (!validateDataType(row, errors, templateHeader, cellValue)) {
                continue;
            }
            //DB Validation
            validateDatabase(row, templateHeaders, errors, templateHeader, cellValue);
        }
    }

    private void validateDatabase(Row row, Set<TemplateHeader> templateHeaders, Map<Integer, List<String>> errors, TemplateHeader templateHeader, String cellValue) {
        if (templateHeader.getDbValidationType() == null) {
            return;
        }
        String[] params = getParams(templateHeader, templateHeaders, row);
        AppEnums.DbValidationType dbValidationType;
        try {
            dbValidationType = AppEnums.DbValidationType.valueOf(templateHeader.getDbValidationType());
        } catch (IllegalArgumentException e) {
            log.error("Invalid db validation type {}", templateHeader.getDbValidationType());
            throw new IllegalArgumentException("Invalid db validation: " + templateHeader.getDbValidationType());
        }
        boolean dbValidationResult = executeDBValidation(templateHeader.getDbValidationQuery(), params);
        if (dbValidationType == AppEnums.DbValidationType.DB_EXISTS && !dbValidationResult) {
            addError(row, errors, templateHeader.getHeaderName() + " has invalid value: " + cellValue);
        } else if (dbValidationType == AppEnums.DbValidationType.DB_DUPLICATE && dbValidationResult) {
            addError(row, errors, templateHeader.getHeaderName() + " already exists: " + cellValue);
        }
    }

    private boolean validateDataType(Row row, Map<Integer, List<String>> errors, TemplateHeader templateHeader, String cellValue) {
        if (templateHeader.getDataType() != null) {
            AppEnums.DataType dataType = AppEnums.DataType.valueOf(templateHeader.getDataType());
            if (dataType == AppEnums.DataType.NUMBER) {
                try {
                    Double.parseDouble(cellValue);
                } catch (NumberFormatException e) {
                    addError(row, errors, templateHeader.getHeaderName() + " should be a number");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.P_NUMBER) {
                try {
                    double value = Double.parseDouble(cellValue);
                    if (value < 0) {
                        addError(row, errors, templateHeader.getHeaderName() + " should be zero or a positive number");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    addError(row, errors, templateHeader.getHeaderName() + " should be zero or a positive number");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.AMOUNT) {
                try {
                    new BigDecimal(cellValue);
                } catch (NumberFormatException e) {
                    addError(row, errors, templateHeader.getHeaderName() + " should be a valid amount");
                    return false;
                }
            } else if (dataType == AppEnums.DataType.DATE) {
                LocalDate date = DateUtils.toLocalDate(cellValue);
                if (date == null) {
                    addError(row, errors, templateHeader.getHeaderName() + " should be in dd-MM-yyyy format");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateLength(Row row, Map<Integer, List<String>> errors, TemplateHeader templateHeader, String cellValue) {
        //Min Length
        if (templateHeader.getMinLength() != null && cellValue.length() < templateHeader.getMinLength()) {
            addError(row, errors, templateHeader.getHeaderName() + " should be at least " + templateHeader.getMinLength() + " characters");
            return false;
        }
        //Max Length
        if (templateHeader.getMaxLength() != null && cellValue.length() > templateHeader.getMaxLength()) {
            addError(row, errors, templateHeader.getHeaderName() + " should not exceed " + templateHeader.getMaxLength() + " characters");
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
                    TemplateHeader templateHeader,
                    Set<TemplateHeader> templateHeaders,
                    Row row
            ) {
        String[] paramsColumnNames = templateHeader.getDbValidationColumns().split(COMMA);
        List<String> paramsColumnValues = new ArrayList<>();
        for (String columnName : paramsColumnNames) {
            templateHeaders.stream()
                    .filter(metadata -> metadata.getHeaderName().equals(columnName.trim()))
                    .map(TemplateHeader::getColumnIndex)
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
