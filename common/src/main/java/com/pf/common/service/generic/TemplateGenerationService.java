package com.pf.common.service.generic;

import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.generic.TemplateAdditionalSheet;
import com.pf.common.entity.generic.TemplateHeader;
import com.pf.common.entity.generic.TemplateName;
import com.pf.common.enums.AppEnums;
import com.pf.common.enums.RefObjectNames;
import com.pf.common.record.generic.SelectItem;
import com.pf.common.repository.categoryManagement.UserCategoryRepository;
import com.pf.common.repository.generic.TemplateNameRepository;
import com.pf.common.repository.setting.ReferenceObjectRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.pf.common.constants.CommonConstants.BLANK_TEXT;
import static com.pf.common.constants.CommonConstants.NO;
import static com.pf.common.constants.CommonConstants.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateGenerationService extends BaseService {

    private static final String DATA_DICTIONARY_SHEET = "Data Dictionary";
    public static final String EXCEL_HEADER = "Excel Header";
    public static final String REQUIRED = "Required";
    public static final String MIN_LENGTH = "Min Length";
    public static final String MAX_LENGTH = "Max Length";
    public static final String DATA_FORMAT = "Data Format";
    public static final String DATA_PATTERN = "Data Pattern";
    public static final String DESCRIPTION = "Description";

    private static final List<String> DATA_DICTIONARY_HEADERS = List.of(EXCEL_HEADER, REQUIRED, MIN_LENGTH, MAX_LENGTH, DATA_FORMAT, DATA_PATTERN, DESCRIPTION);

    private final TemplateNameRepository templateNamesRepository;
    private final ReferenceObjectRepository referenceObjectRepository;
    private final ReferenceValueRepository referenceValueRepository;
    private final UserCategoryRepository userCategoryRepository;

    @Transactional
    public ByteArrayInputStream generateTemplate(String templateName) {
        TemplateName template = templateNamesRepository.findByTemplateName(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        return createWorkbook(template);
    }

    private ByteArrayInputStream createWorkbook(TemplateName template) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle requiredHeaderStyle = ExcelUtils.createHeaderStyle(workbook, true);
            CellStyle optionalHeaderStyle = ExcelUtils.createHeaderStyle(workbook, false);
            CellStyle dictionaryBodyStyle = ExcelUtils.createDictionaryBodyStyle(workbook);
            createMainSheetAndHeaders(workbook, template, requiredHeaderStyle, optionalHeaderStyle);
            createDataDictionarySheet(workbook, template, dictionaryBodyStyle);
            createAdditionalSheets(workbook, template);
            int activeSheetIndex = workbook.getSheetIndex(template.getSheetName());
            if (activeSheetIndex >= 0) {
                workbook.setActiveSheet(activeSheetIndex);
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Error creating Excel template for templateName={}", template.getTemplateName(), e);
            throw new IllegalStateException("Unable to create Excel template for: " + template.getTemplateName(), e);
        }
    }

    private void createMainSheetAndHeaders(XSSFWorkbook workbook, TemplateName template, CellStyle requiredHeaderStyle, CellStyle optionalHeaderStyle) {
        XSSFSheet sheet = workbook.createSheet(template.getSheetName());
        Row headerRow = sheet.createRow(0);
        Set<TemplateHeader> templateHeaders = template.getTemplateHeaders();
        for (TemplateHeader header : templateHeaders) {
            Cell cell = headerRow.createCell(header.getColumnIndex());
            cell.setCellValue(header.getHeaderName());
            cell.setCellStyle(header.isRequired() ? requiredHeaderStyle : optionalHeaderStyle);
            ExcelUtils.setMaxWidth(header.getColumnIndex(), sheet);
        }
        headerRow.setHeightInPoints(25);
        sheet.createFreezePane(0, 1);
    }

    private void createDataDictionarySheet(XSSFWorkbook workbook, TemplateName template, CellStyle dictionaryBodyStyle) {
        XSSFSheet sheet = createDataDictionarySheetHeaders(workbook);
        createDataDictionaryData(sheet, template, dictionaryBodyStyle);
    }

    private XSSFSheet createDataDictionarySheetHeaders(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(DATA_DICTIONARY_SHEET);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = ExcelUtils.createHeaderStyle(workbook, false);
        for (int cellIndex = 0; cellIndex < DATA_DICTIONARY_HEADERS.size(); cellIndex++) {
            Cell cell = headerRow.createCell(cellIndex);
            cell.setCellValue(DATA_DICTIONARY_HEADERS.get(cellIndex));
            cell.setCellStyle(headerStyle);
        }
        headerRow.setHeightInPoints(25);
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    private void createDataDictionaryData(XSSFSheet sheet, TemplateName template, CellStyle dictionaryBodyStyle) {
        int rowIndex = 1;
        for (TemplateHeader header : template.getTemplateHeaders().stream().sorted(Comparator.comparing(TemplateHeader::getColumnIndex)).toList()) {
            Row row = sheet.createRow(rowIndex++);
            createDictionaryCell(row, 0, header.getHeaderName(), dictionaryBodyStyle);
            createDictionaryCell(row, 1, header.isRequired() ? YES : NO, dictionaryBodyStyle);
            createDictionaryCell(row, 2, header.getMinLength() != null ? header.getMinLength().toString() : BLANK_TEXT, dictionaryBodyStyle);
            createDictionaryCell(row, 3, header.getMaxLength() != null ? header.getMaxLength().toString() : BLANK_TEXT, dictionaryBodyStyle);
            createDictionaryCell(row, 4, header.getDataType() != null ? header.getDataType() : BLANK_TEXT, dictionaryBodyStyle);
            createDictionaryCell(row, 5, header.getRegexPattern() != null ? header.getRegexPattern() : BLANK_TEXT, dictionaryBodyStyle);
            createDictionaryCell(row, 6, header.getDescription() != null ? header.getDescription() : BLANK_TEXT, dictionaryBodyStyle);
            row.setHeightInPoints(55);
        }
        for (int columnIndex = 0; columnIndex < DATA_DICTIONARY_HEADERS.size(); columnIndex++) {
            ExcelUtils.setMaxWidth(columnIndex, sheet);
        }
    }

    private void createDictionaryCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : BLANK_TEXT);
        cell.setCellStyle(style);
    }

    private void createAdditionalSheets(XSSFWorkbook workbook, TemplateName template) {
        for (TemplateAdditionalSheet additionalSheets : template.getTemplateAdditionalSheets().stream().sorted(Comparator.comparing(TemplateAdditionalSheet::getId)).toList()) {
            createAdditionalSheet(workbook, additionalSheets);
        }
    }

    private void createAdditionalSheet(XSSFWorkbook workbook, TemplateAdditionalSheet template) {
        Map<String, List<String>> additionalSheetData = new TreeMap<>(fetchAdditionalSheetData(template.getDataReference()));
        XSSFSheet sheet = workbook.createSheet(template.getSheetName());
        CellStyle headerStyle = ExcelUtils.createHeaderStyle(workbook, false);
        Row headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (String headerName : additionalSheetData.keySet()) {
            Cell headerCell = headerRow.createCell(columnIndex);
            headerCell.setCellValue(headerName);
            headerCell.setCellStyle(headerStyle);
            columnIndex++;
        }
        headerRow.setHeightInPoints(25);
        int maxRows = additionalSheetData.values().stream().mapToInt(List::size).max().orElse(0);
        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            columnIndex = 0;
            for (List<String> columnData : additionalSheetData.values()) {
                Cell cell = row.createCell(columnIndex);
                String value = rowIndex < columnData.size() ? columnData.get(rowIndex) : null;
                cell.setCellValue(value != null ? value : BLANK_TEXT);
                columnIndex++;
            }
        }
        for (columnIndex = 0; columnIndex < additionalSheetData.size(); columnIndex++) {
            ExcelUtils.setMaxWidth(columnIndex, sheet);
        }
        sheet.createFreezePane(0, 1);
    }

    private Map<String, List<String>> fetchAdditionalSheetData(String dataReference) {
        AppEnums.DataReference reference = AppEnums.DataReference.valueOf(dataReference);
        return switch (reference) {
            case REFERENCE_OBJECT -> fetchReferenceObjectNames();
            case TRANSACTION_TYPE -> fetchTransactionTypes();
            case CATEGORY -> fetchUserCategories();
            case SUB_CATEGORY -> fetchUserSubcategories();
            case LOCATION -> fetchLocations();
        };
    }

    private Map<String, List<String>> fetchReferenceObjectNames() {
        List<String> referenceObjectNames = referenceObjectRepository.findRefObjNamesOrderByRefObjNameAsc();
        Map<String, List<String>> referenceObjectNameMap = new HashMap<>();
        referenceObjectNameMap.put("Reference Object", referenceObjectNames);
        return referenceObjectNameMap;
    }

    private Map<String, List<String>> fetchTransactionTypes() {
        List<String> transactionTypes =
                referenceValueRepository
                        .findReferenceCodeByRefObjName(RefObjectNames.TRANSACTION_TYPE.name())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
        Map<String, List<String>> transactionTypeMap = new HashMap<>();
        transactionTypeMap.put("Transaction Type", transactionTypes);
        return transactionTypeMap;
    }

    private Map<String, List<String>> fetchLocations() {
        List<String> transactionTypes =
                referenceValueRepository
                        .findReferenceCodeByRefObjName(RefObjectNames.LOCATION.name())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
        Map<String, List<String>> transactionTypeMap = new HashMap<>();
        transactionTypeMap.put("Location", transactionTypes);
        return transactionTypeMap;
    }

    private Map<String, List<String>> fetchUserCategories() {
        List<SelectItem> categories =
                userCategoryRepository
                        .findCategoriesByUserId(fetchLoginUser().getId());
        return categories.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.value() != null)
                .filter(item -> item.label() != null)
                .collect(
                        Collectors.groupingBy(
                                item -> item.value().toString(),
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        SelectItem::label,
                                        Collectors.collectingAndThen(Collectors.toList(),
                                                list -> list.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList())
                                )
                        )
                );
    }

    private Map<String, List<String>> fetchUserSubcategories() {
        List<UserCategory> categories =
                userCategoryRepository
                        .findAllCategoriesByUserId(fetchLoginUser().getId());
        return categories.stream()
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toMap(
                                UserCategory::getCategoryName,
                                userCategory -> userCategory.getSubcategories().stream()
                                        .map(UserSubcategory::getSubcategoryName).sorted(String.CASE_INSENSITIVE_ORDER)
                                        .collect(Collectors.toList())
                        )
                );
    }
}