package com.pf.common.repository.bulkUpload;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.enums.RefObjectNames;
import com.pf.common.repository.categoryManagement.UserCategoryRepository;
import com.pf.common.repository.setting.ReferenceObjectRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadTemplateService extends BaseService {

    public static final String REFERENCE_VALUES = "Reference Values";
    private static final String CATEGORIES = "Categories";
    private static final String SUBCATEGORIES = "Subcategories";
    private static final String FINANCIAL_TRANSACTIONS = "Financial Transactions";
    private static final String REFERENCE_OBJECTS = "Reference Objects";
    private static final String TRANSACTION_TYPES = "Transaction Types";
    private static final String LOCATIONS = "Locations";
    private static final String DATA_DICTIONARY_SHEET = "Data Dictionary";

    /*
     * Excel header names.
     *
     * '*' indicates a required field.
     */
    private static final String[] REFERENCE_VALUE_HEADERS = {
            "Reference Object *",
            "Reference Code *",
            "Reference Code Description *",
            "Reference Code 2",
            "Reference Code 3"
    };

    private static final String[] CATEGORY_HEADERS = {
            "Transaction Type *",
            "Category Name *",
            "Category Description *"
    };

    private static final String[] SUBCATEGORY_HEADERS = {
            "Transaction Type *",
            "Category Name *",
            "Subcategory Name *",
            "Subcategory Description *"
    };

    private static final String[] FINANCIAL_TRANSACTIONS_HEADERS = {
            "Transaction Date *",
            "Amount *",
            "Transaction Type *",
            "Category Name *",
            "Subcategory Name *",
            "Subcategory Description *",
            "Location",
            "Remarks"
    };

    private static final String[] DATA_DICTIONARY_HEADERS = {
            "Excel Header",
            "Database Column",
            "Required",
            "Description"
    };


    private final ReferenceObjectRepository referenceObjectRepository;
    private final ReferenceValueRepository referenceValueRepository;
    private final UserCategoryRepository userCategoryRepository;

    /**
     * Generates the Reference Values Excel template.
     */
    public ByteArrayInputStream generateReferenceValuesTemplate() {

        List<String> referenceObjectNames =
                referenceObjectRepository
                        .findRefObjNamesOrderByRefObjNameAsc()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .toList();

        log.info("Generating Reference Values template.");

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            // =========================================================
            // Main sheet
            // =========================================================

            XSSFSheet sheet =
                    workbook.createSheet(REFERENCE_VALUES);

            createHeader(
                    sheet,
                    workbook,
                    REFERENCE_VALUE_HEADERS
            );

            sheet.createFreezePane(0, 1);

            // =========================================================
            // Reference Objects sheet
            // =========================================================

            XSSFSheet lookupSheet =
                    workbook.createSheet(REFERENCE_OBJECTS);

            createReferenceObjectLookup(
                    lookupSheet,
                    referenceObjectNames
            );

            if (referenceObjectNames.isEmpty()) {

                log.warn(
                        "No reference objects found in reference_object table."
                );
            }

            /*
             * IMPORTANT:
             *
             * Do NOT hide this sheet.
             *
             * User needs to open this sheet and check the valid
             * Reference Object names before entering data.
             */

            // =========================================================
            // Main sheet column widths
            // =========================================================

            setReferenceValueColumnWidths(sheet);

            // =========================================================
            // Data Dictionary
            // =========================================================

            createReferenceValueDataDictionarySheet(workbook);

            // =========================================================
            // Open Reference Values sheet by default
            // =========================================================

            workbook.setActiveSheet(
                    workbook.getSheetIndex(REFERENCE_VALUES)
            );

            // =========================================================
            // Write workbook
            // =========================================================

            workbook.write(outputStream);

            log.info(
                    "Reference Values template generated successfully."
            );

            return new ByteArrayInputStream(
                    outputStream.toByteArray()
            );

        } catch (IOException e) {

            log.error(
                    "Failed to generate Reference Values template",
                    e
            );

            throw new IllegalStateException(
                    "Failed to generate Reference Values template",
                    e
            );
        }
    }

    /**
     * Creates the main Reference Values header.
     * <p>
     * Required headers:
     * - Grey background
     * - Red text (#f44336)
     * <p>
     * Optional headers:
     * - Grey background
     * - Black text
     */
    private void createHeader(
            XSSFSheet sheet,
            XSSFWorkbook workbook,
            String[] headers
    ) {

        Row headerRow =
                sheet.createRow(0);

        CellStyle requiredHeaderStyle =
                createHeaderStyle(
                        workbook,
                        true
                );

        CellStyle optionalHeaderStyle =
                createHeaderStyle(
                        workbook,
                        false
                );

        for (int i = 0; i < headers.length; i++) {

            Cell cell =
                    headerRow.createCell(i);

            String header =
                    headers[i];

            cell.setCellValue(header);

            if (header.endsWith("*")) {

                cell.setCellStyle(
                        requiredHeaderStyle
                );

            } else {

                cell.setCellStyle(
                        optionalHeaderStyle
                );
            }
        }

        headerRow.setHeightInPoints(25);
    }

    /**
     * Creates the header style.
     * <p>
     * Required:
     * Text = Angular Material error color #f44336
     * <p>
     * Optional:
     * Text = Black
     * <p>
     * Both:
     * Background = Grey
     */
    private CellStyle createHeaderStyle(
            XSSFWorkbook workbook,
            boolean required
    ) {

        CellStyle headerStyle =
                workbook.createCellStyle();

        XSSFFont font =
                workbook.createFont();

        font.setBold(true);

        if (required) {

            /*
             * Angular Material error/warn color:
             *
             * #f44336
             */
            XSSFColor errorColor =
                    new XSSFColor(
                            new byte[]{
                                    (byte) 0xF4,
                                    (byte) 0x43,
                                    (byte) 0x36
                            },
                            null
                    );

            font.setColor(errorColor);

        } else {

            font.setColor(
                    IndexedColors.BLACK.getIndex()
            );
        }

        headerStyle.setFont(font);

        /*
         * Grey background.
         */
        headerStyle.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        headerStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        headerStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        headerStyle.setBorderTop(
                BorderStyle.THIN
        );

        headerStyle.setBorderBottom(
                BorderStyle.THIN
        );

        headerStyle.setBorderLeft(
                BorderStyle.THIN
        );

        headerStyle.setBorderRight(
                BorderStyle.THIN
        );

        return headerStyle;
    }

    /**
     * Creates the visible Reference Objects sheet.
     * <p>
     * This sheet contains all valid ref_obj_name values from
     * the reference_object table.
     * <p>
     * Users can refer to this sheet while entering the
     * Reference Object value in the Reference Values sheet.
     */
    private void createReferenceObjectLookup(
            XSSFSheet lookupSheet,
            List<String> referenceObjectNames
    ) {

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                lookupSheet.createRow(0);

        Cell headerCell =
                headerRow.createCell(0);

        headerCell.setCellValue(
                "Reference Object Name"
        );

        CellStyle headerStyle =
                createLookupHeaderStyle(
                        lookupSheet.getWorkbook()
                );

        headerCell.setCellStyle(
                headerStyle
        );

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Reference Object values
        // =========================================================

        for (
                int i = 0;
                i < referenceObjectNames.size();
                i++
        ) {

            Row row =
                    lookupSheet.createRow(i + 1);

            Cell cell =
                    row.createCell(0);

            cell.setCellValue(
                    referenceObjectNames.get(i)
            );
        }

        lookupSheet.setColumnWidth(
                0,
                40 * 256
        );

        lookupSheet.createFreezePane(0, 1);

        log.debug(
                "Reference Objects sheet created with {} values.",
                referenceObjectNames.size()
        );
    }

    /**
     * Creates the visible Transaction Types sheet.
     * <p>
     * This sheet contains all valid reference_code values from
     * the reference_value table.
     * <p>
     * Users can refer to this sheet while entering the
     * Transaction Type value in the Category sheet.
     */
    private void createTransactionTypesLookup(
            XSSFSheet lookupSheet,
            List<String> transactionTypes
    ) {

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                lookupSheet.createRow(0);

        Cell headerCell =
                headerRow.createCell(0);

        headerCell.setCellValue(
                "Transaction Type"
        );

        CellStyle headerStyle =
                createLookupHeaderStyle(
                        lookupSheet.getWorkbook()
                );

        headerCell.setCellStyle(
                headerStyle
        );

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Reference Object values
        // =========================================================

        for (
                int i = 0;
                i < transactionTypes.size();
                i++
        ) {

            Row row =
                    lookupSheet.createRow(i + 1);

            Cell cell =
                    row.createCell(0);

            cell.setCellValue(
                    transactionTypes.get(i)
            );
        }

        lookupSheet.setColumnWidth(
                0,
                40 * 256
        );

        lookupSheet.createFreezePane(0, 1);

        log.debug(
                "Transaction Types sheet created with {} values.",
                transactionTypes.size()
        );
    }

    /**
     * Creates the visible Locations sheet.
     */
    private void createLocationsLookup(
            XSSFSheet lookupSheet,
            List<SelectItem> locations
    ) {

        // =========================================================
        // Header
        // =========================================================
        Row headerRow = lookupSheet.createRow(0);

        Cell locationHeaderCell = headerRow.createCell(0);
        locationHeaderCell.setCellValue("Location");

        Cell descriptionHeaderCell = headerRow.createCell(1);
        descriptionHeaderCell.setCellValue("Description");

        CellStyle headerStyle = createLookupHeaderStyle(
                lookupSheet.getWorkbook()
        );

        locationHeaderCell.setCellStyle(headerStyle);
        descriptionHeaderCell.setCellStyle(headerStyle);

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Locations
        // =========================================================
        for (int i = 0; i < locations.size(); i++) {

            SelectItem item = locations.get(i);

            Row row = lookupSheet.createRow(i + 1);

            // Location -> SelectItem.value
            Cell locationCell = row.createCell(0);
            locationCell.setCellValue(
                    item.value() != null
                            ? item.value().toString()
                            : ""
            );

            // Description -> SelectItem.label
            Cell descriptionCell = row.createCell(1);
            descriptionCell.setCellValue(
                    item.label() != null
                            ? item.label()
                            : ""
            );
        }

        // =========================================================
        // Column widths
        // =========================================================
        lookupSheet.setColumnWidth(0, 40 * 256);
        lookupSheet.setColumnWidth(1, 60 * 256);

        lookupSheet.createFreezePane(0, 1);

        log.debug(
                "Locations sheet created with {} values.",
                locations.size()
        );
    }


    /**
     * Creates the header style for the Reference Objects sheet.
     */
    private CellStyle createLookupHeaderStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors.BLACK.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        return style;
    }

    /**
     * Creates the Reference Value Data Dictionary sheet.
     */
    private void createReferenceValueDataDictionarySheet(
            XSSFWorkbook workbook
    ) {

        XSSFSheet dictionarySheet =
                workbook.createSheet(
                        DATA_DICTIONARY_SHEET
                );

        // =========================================================
        // Styles
        // =========================================================

        CellStyle headerStyle =
                createDictionaryHeaderStyle(
                        workbook
                );

        CellStyle bodyStyle =
                createDictionaryBodyStyle(
                        workbook
                );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                dictionarySheet.createRow(0);

        for (
                int i = 0;
                i < DATA_DICTIONARY_HEADERS.length;
                i++
        ) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    DATA_DICTIONARY_HEADERS[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Dictionary data
        // =========================================================

        String[][] dictionaryData = {

                {
                        "Reference Object *",
                        "ref_obj_name",
                        "Yes",
                        "Reference object name. " +
                                "Enter a valid reference object name " +
                                "from the 'Reference Objects' sheet."
                },

                {
                        "Reference Code *",
                        "reference_code",
                        "Yes",
                        "Reference code associated with the " +
                                "selected reference object."
                },

                {
                        "Reference Code Description *",
                        "reference_code_description",
                        "Yes",
                        "Description of the reference code."
                },

                {
                        "Reference Code 2",
                        "reference_code_2",
                        "No",
                        "Optional secondary reference code."
                },

                {
                        "Reference Code 3",
                        "reference_code_3",
                        "No",
                        "Optional tertiary reference code."
                }
        };

        for (
                int rowIndex = 0;
                rowIndex < dictionaryData.length;
                rowIndex++
        ) {

            Row row =
                    dictionarySheet.createRow(
                            rowIndex + 1
                    );

            row.setHeightInPoints(55);

            for (
                    int columnIndex = 0;
                    columnIndex < dictionaryData[rowIndex].length;
                    columnIndex++
            ) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellValue(
                        dictionaryData[rowIndex][columnIndex]
                );

                cell.setCellStyle(
                        bodyStyle
                );
            }
        }

        // =========================================================
        // Formatting
        // =========================================================

        dictionarySheet.createFreezePane(
                0,
                1
        );

        dictionarySheet.setColumnWidth(
                0,
                35 * 256
        );

        dictionarySheet.setColumnWidth(
                1,
                40 * 256
        );

        dictionarySheet.setColumnWidth(
                2,
                15 * 256
        );

        dictionarySheet.setColumnWidth(
                3,
                90 * 256
        );

        log.debug(
                "Data Dictionary sheet created."
        );
    }

    /**
     * Creates the Category Data Dictionary sheet.
     */
    private void createCategoryDataDictionarySheet(
            XSSFWorkbook workbook
    ) {

        XSSFSheet dictionarySheet =
                workbook.createSheet(
                        DATA_DICTIONARY_SHEET
                );

        // =========================================================
        // Styles
        // =========================================================

        CellStyle headerStyle =
                createDictionaryHeaderStyle(
                        workbook
                );

        CellStyle bodyStyle =
                createDictionaryBodyStyle(
                        workbook
                );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                dictionarySheet.createRow(0);

        for (
                int i = 0;
                i < DATA_DICTIONARY_HEADERS.length;
                i++
        ) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    DATA_DICTIONARY_HEADERS[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Dictionary data
        // =========================================================

        String[][] dictionaryData = {

                {
                        "Transaction Type *",
                        "transaction_type",
                        "Yes",
                        "Reference value. " +
                                "Enter a valid transaction type " +
                                "from the 'Transaction Types' sheet."
                },

                {
                        "Category Name *",
                        "category_name",
                        "Yes",
                        "Category name associated with the " +
                                "selected reference object."
                },

                {
                        "Category Description *",
                        "category_description",
                        "Yes",
                        "Description of the category."
                },
        };

        for (
                int rowIndex = 0;
                rowIndex < dictionaryData.length;
                rowIndex++
        ) {

            Row row =
                    dictionarySheet.createRow(
                            rowIndex + 1
                    );

            row.setHeightInPoints(55);

            for (
                    int columnIndex = 0;
                    columnIndex < dictionaryData[rowIndex].length;
                    columnIndex++
            ) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellValue(
                        dictionaryData[rowIndex][columnIndex]
                );

                cell.setCellStyle(
                        bodyStyle
                );
            }
        }

        // =========================================================
        // Formatting
        // =========================================================

        dictionarySheet.createFreezePane(
                0,
                1
        );

        dictionarySheet.setColumnWidth(
                0,
                35 * 256
        );

        dictionarySheet.setColumnWidth(
                1,
                40 * 256
        );

        dictionarySheet.setColumnWidth(
                2,
                15 * 256
        );

        dictionarySheet.setColumnWidth(
                3,
                90 * 256
        );

        log.debug("Category Data Dictionary sheet created.");
    }

    /**
     * Data Dictionary header style.
     */
    private CellStyle createDictionaryHeaderStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();

        Font font =
                workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors.BLACK.getIndex()
        );

        style.setFont(font);

        style.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        return style;
    }

    /**
     * Data Dictionary body style.
     */
    private CellStyle createDictionaryBodyStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();

        style.setWrapText(true);

        style.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );

        return style;
    }

    /**
     * Sets column widths for Reference Values sheet.
     */
    private void setReferenceValueColumnWidths(
            XSSFSheet sheet
    ) {

        // Reference Object
        sheet.setColumnWidth(
                0,
                30 * 256
        );

        // Reference Code
        sheet.setColumnWidth(
                1,
                25 * 256
        );

        // Reference Code Description
        sheet.setColumnWidth(
                2,
                50 * 256
        );

        // Reference Code 2
        sheet.setColumnWidth(
                3,
                25 * 256
        );

        // Reference Code 3
        sheet.setColumnWidth(
                4,
                25 * 256
        );
    }

    /**
     * Sets column widths for Category sheet.
     */
    private void setCategoryColumnWidths(
            XSSFSheet sheet
    ) {

        // Reference Value
        sheet.setColumnWidth(
                0,
                30 * 256
        );

        // Category Name
        sheet.setColumnWidth(
                1,
                25 * 256
        );

        // Category Description
        sheet.setColumnWidth(
                2,
                50 * 256
        );
    }

    // ============================================================
    // Categories template
    // ============================================================

    public ByteArrayInputStream generateCategoriesTemplate() {
        {

            List<String> transactionTypes =
                    referenceValueRepository
                            .findReferenceCodeByRefObjName(RefObjectNames.TRANSACTION_TYPE.name())
                            .stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .distinct()
                            .toList();

            log.info("Generating Category template.");

            try (
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    ByteArrayOutputStream outputStream =
                            new ByteArrayOutputStream()
            ) {

                // =========================================================
                // Main sheet
                // =========================================================

                XSSFSheet sheet =
                        workbook.createSheet(CATEGORIES);

                createHeader(
                        sheet,
                        workbook,
                        CATEGORY_HEADERS
                );

                sheet.createFreezePane(0, 1);

                // =========================================================
                // Transaction Types sheet
                // =========================================================

                XSSFSheet lookupSheet =
                        workbook.createSheet(TRANSACTION_TYPES);

                createTransactionTypesLookup(
                        lookupSheet,
                        transactionTypes
                );

                if (transactionTypes.isEmpty()) {

                    log.warn("No transaction types found in reference_value table.");
                }

                /*
                 * IMPORTANT:
                 *
                 * Do NOT hide this sheet.
                 *
                 * User needs to open this sheet and check the valid
                 * Transaction Types before entering data.
                 */

                // =========================================================
                // Main sheet column widths
                // =========================================================

                setCategoryColumnWidths(sheet);

                // =========================================================
                // Data Dictionary
                // =========================================================

                createCategoryDataDictionarySheet(workbook);

                // =========================================================
                // Open Categories sheet by default
                // =========================================================

                workbook.setActiveSheet(
                        workbook.getSheetIndex(CATEGORIES)
                );

                // =========================================================
                // Write workbook
                // =========================================================

                workbook.write(outputStream);

                log.info(
                        "Category template generated successfully."
                );

                return new ByteArrayInputStream(
                        outputStream.toByteArray()
                );

            } catch (IOException e) {

                log.error(
                        "Failed to generate Category template",
                        e
                );

                throw new IllegalStateException(
                        "Failed to generate Category template",
                        e
                );
            }
        }
    }

    public ByteArrayInputStream generateSubcategoriesTemplate() {

        List<String> transactionTypes =
                referenceValueRepository
                        .findReferenceCodeByRefObjName(
                                RefObjectNames.TRANSACTION_TYPE.name()
                        )
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .toList();

        List<SelectItem> categories =
                userCategoryRepository
                        .findCategoriesByUserId(fetchLoginUser().getId());

        log.info("Generating Subcategory template.");

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            // =========================================================
            // Main sheet
            // =========================================================

            XSSFSheet sheet =
                    workbook.createSheet(SUBCATEGORIES);

            createHeader(
                    sheet,
                    workbook,
                    SUBCATEGORY_HEADERS
            );

            sheet.createFreezePane(0, 1);

            // =========================================================
            // Transaction Types sheet
            // =========================================================

            XSSFSheet transactionTypesSheet =
                    workbook.createSheet(TRANSACTION_TYPES);

            createTransactionTypesLookup(
                    transactionTypesSheet,
                    transactionTypes
            );

            if (transactionTypes.isEmpty()) {
                log.warn(
                        "No transaction types found in reference_value table ."
                );
            }

            // =========================================================
            // Categories sheet
            // =========================================================

            XSSFSheet categoriesSheet =
                    workbook.createSheet(CATEGORIES);

            createCategoryLookup(
                    categoriesSheet,
                    categories
            );

            if (categories.isEmpty()) {

                log.warn(
                        "No active categories found for userId={}",
                        fetchLoginUser().getId()
                );
            }

            // =========================================================
            // Main sheet column widths
            // =========================================================

            setSubcategoryColumnWidths(sheet);

            // =========================================================
            // Data Dictionary
            // =========================================================

            createSubcategoryDataDictionarySheet(workbook);

            // =========================================================
            // Open Subcategories sheet by default
            // =========================================================

            workbook.setActiveSheet(
                    workbook.getSheetIndex(SUBCATEGORIES)
            );

            // =========================================================
            // Write workbook
            // =========================================================

            workbook.write(outputStream);

            log.info(
                    "Subcategory template generated successfully."
            );

            return new ByteArrayInputStream(
                    outputStream.toByteArray()
            );

        } catch (IOException e) {

            log.error(
                    "Failed to generate Subcategory template",
                    e
            );

            throw new IllegalStateException(
                    "Failed to generate Subcategory template",
                    e
            );
        }
    }

    public ByteArrayInputStream generateFinancialTransactionsTemplate() {

        List<String> transactionTypes =
                referenceValueRepository
                        .findReferenceCodeByRefObjName(
                                RefObjectNames.TRANSACTION_TYPE.name()
                        )
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .toList();

        List<SelectItem> locations =
                referenceValueRepository
                        .fetchReferenceCodeAndReferenceDescriptionByRefObjName(
                                RefObjectNames.LOCATION.name()
                        )
                        .stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<SelectItem> categories =
                userCategoryRepository
                        .findCategoriesByUserId(fetchLoginUser().getId());

        List<UserCategory> allCategories =
                userCategoryRepository
                        .findAllCategoriesByUserId(fetchLoginUser().getId());

        log.info("Generating Financial Transaction template.");

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            // =========================================================
            // Main sheet
            // =========================================================

            XSSFSheet sheet =
                    workbook.createSheet(FINANCIAL_TRANSACTIONS);

            createHeader(
                    sheet,
                    workbook,
                    FINANCIAL_TRANSACTIONS_HEADERS
            );

            sheet.createFreezePane(0, 1);

            // =========================================================
            // Transaction Types sheet
            // =========================================================

            XSSFSheet transactionTypesSheet =
                    workbook.createSheet(TRANSACTION_TYPES);

            createTransactionTypesLookup(
                    transactionTypesSheet,
                    transactionTypes
            );


            if (transactionTypes.isEmpty()) {
                log.warn(
                        "No transaction types found in reference_value table  ."
                );
            }

            // =========================================================
            // Categories sheet
            // =========================================================

            XSSFSheet categoriesSheet =
                    workbook.createSheet(CATEGORIES);

            createCategoryLookup(
                    categoriesSheet,
                    categories
            );

            if (categories.isEmpty()) {

                log.warn(
                        "No active categories found for userId ={}",
                        fetchLoginUser().getId()
                );
            }

            // =========================================================
            // Subcategories sheet
            // =========================================================

            XSSFSheet subcategoriesSheet =
                    workbook.createSheet(SUBCATEGORIES);

            createSubcategoryLookup(
                    subcategoriesSheet,
                    allCategories
            );

            // =========================================================
            // Locations
            // =========================================================

            XSSFSheet locationsSheet =
                    workbook.createSheet(LOCATIONS);

            createLocationsLookup(
                    locationsSheet,
                    locations
            );

            // =========================================================
            // Main sheet column widths
            // =========================================================

            setFinancialTransactionsColumnWidths(sheet);

            // =========================================================
            // Data Dictionary
            // =========================================================

            createFinancialTransactionDataDictionarySheet(workbook);

            // =========================================================
            // Open Subcategories sheet by default
            // =========================================================

            workbook.setActiveSheet(
                    workbook.getSheetIndex(FINANCIAL_TRANSACTIONS)
            );

            // =========================================================
            // Write workbook
            // =========================================================

            workbook.write(outputStream);

            log.info(
                    "Financial Transactions template generated successfully."
            );

            return new ByteArrayInputStream(
                    outputStream.toByteArray()
            );

        } catch (IOException e) {

            log.error(
                    "Failed to generate Financial Transactions template",
                    e
            );

            throw new IllegalStateException(
                    "Failed to generate Financial Transactions template",
                    e
            );
        }
    }

    private void createCategoryLookup(
            XSSFSheet lookupSheet,
            List<SelectItem> categories
    ) {

        /*
         * Group categories by SelectItem.value().
         *
         * Example:
         *
         * value = "INCOME"
         *   Salary
         *   Bonus
         *
         * value = "EXPENSE"
         *   Food
         *   Travel
         *
         * Excel:
         *
         * | INCOME | EXPENSE |
         * | Salary | Food    |
         * | Bonus  | Travel  |
         */

        Map<String, List<String>> categoriesByValue =
                categories.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item.value() != null)
                        .filter(item -> item.label() != null)
                        .collect(
                                Collectors.groupingBy(
                                        item -> item.value().toString(),
                                        LinkedHashMap::new,
                                        Collectors.mapping(
                                                SelectItem::label,
                                                Collectors.toList()
                                        )
                                )
                        );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                lookupSheet.createRow(0);

        CellStyle headerStyle =
                createLookupHeaderStyle(
                        lookupSheet.getWorkbook()
                );

        int columnIndex = 0;

        for (String value : categoriesByValue.keySet()) {

            Cell headerCell =
                    headerRow.createCell(columnIndex);

            headerCell.setCellValue(value);

            headerCell.setCellStyle(headerStyle);

            columnIndex++;
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Category values
        // =========================================================

        int maxRows =
                categoriesByValue.values()
                        .stream()
                        .mapToInt(List::size)
                        .max()
                        .orElse(0);

        List<List<String>> groupedCategories =
                new ArrayList<>(
                        categoriesByValue.values()
                );

        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {

            Row row =
                    lookupSheet.createRow(rowIndex + 1);

            for (
                    int colIndex = 0;
                    colIndex < groupedCategories.size();
                    colIndex++
            ) {

                List<String> categoryNames =
                        groupedCategories.get(colIndex);

                if (rowIndex < categoryNames.size()) {

                    Cell cell =
                            row.createCell(colIndex);

                    cell.setCellValue(
                            categoryNames.get(rowIndex)
                    );
                }
            }
        }

        // =========================================================
        // Column widths
        // =========================================================

        for (
                int i = 0;
                i < categoriesByValue.size();
                i++
        ) {

            lookupSheet.setColumnWidth(
                    i,
                    40 * 256
            );
        }

        lookupSheet.createFreezePane(0, 1);

        log.debug(
                "Categories sheet created with {} groups.",
                categoriesByValue.size()
        );
    }

    private void createSubcategoryLookup(
            XSSFSheet lookupSheet,
            List<UserCategory> categories
    ) {

        /*
         * Group categories by subcategory name.
         */

        Map<String, List<String>> categoriesMap =
                categories.stream()
                        .filter(Objects::nonNull)
                        .collect(
                                Collectors.toMap(
                                        UserCategory::getCategoryName,
                                        userCategory -> userCategory.getSubcategories().stream()
                                                .map(UserSubcategory::getSubcategoryName)
                                                .collect(Collectors.toList())
                                )
                        );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                lookupSheet.createRow(0);

        CellStyle headerStyle =
                createLookupHeaderStyle(
                        lookupSheet.getWorkbook()
                );

        int columnIndex = 0;

        for (String value : categoriesMap.keySet()) {

            Cell headerCell =
                    headerRow.createCell(columnIndex);

            headerCell.setCellValue(value);

            headerCell.setCellStyle(headerStyle);

            columnIndex++;
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Category values
        // =========================================================

        int maxRows =
                categoriesMap.values()
                        .stream()
                        .mapToInt(List::size)
                        .max()
                        .orElse(0);

        List<List<String>> subcategories =
                new ArrayList<>(
                        categoriesMap.values()
                );

        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {

            Row row =
                    lookupSheet.createRow(rowIndex + 1);

            for (
                    int colIndex = 0;
                    colIndex < subcategories.size();
                    colIndex++
            ) {

                List<String> categoryNames =
                        subcategories.get(colIndex);

                if (rowIndex < categoryNames.size()) {

                    Cell cell =
                            row.createCell(colIndex);

                    cell.setCellValue(
                            categoryNames.get(rowIndex)
                    );
                }
            }
        }

        // =========================================================
        // Column widths
        // =========================================================

        for (
                int i = 0;
                i < categoriesMap.size();
                i++
        ) {

            lookupSheet.setColumnWidth(
                    i,
                    40 * 256
            );
        }

        lookupSheet.createFreezePane(0, 1);

        log.debug(
                "Subcategories sheet created with {} groups.",
                categoriesMap.size()
        );
    }

    private void setSubcategoryColumnWidths(
            XSSFSheet sheet
    ) {

        // Transaction Type
        sheet.setColumnWidth(
                0,
                30 * 256
        );

        // Category Name
        sheet.setColumnWidth(
                1,
                30 * 256
        );

        // Subcategory Name
        sheet.setColumnWidth(
                2,
                35 * 256
        );

        // Subcategory Description
        sheet.setColumnWidth(
                3,
                50 * 256
        );
    }

    private void setFinancialTransactionsColumnWidths(
            XSSFSheet sheet
    ) {

        // Transaction Date
        sheet.setColumnWidth(
                0,
                30 * 256
        );

        // Amount
        sheet.setColumnWidth(
                1,
                30 * 256
        );

        // Transaction Type
        sheet.setColumnWidth(
                2,
                30 * 256
        );

        // Category Name
        sheet.setColumnWidth(
                3,
                30 * 256
        );

        // Subcategory Name
        sheet.setColumnWidth(
                4,
                35 * 256
        );

        // Location
        sheet.setColumnWidth(
                5,
                35 * 256
        );

        // Remarks
        sheet.setColumnWidth(
                6,
                50 * 256
        );
    }

    private void createSubcategoryDataDictionarySheet(
            XSSFWorkbook workbook
    ) {

        XSSFSheet dictionarySheet =
                workbook.createSheet(
                        DATA_DICTIONARY_SHEET
                );

        CellStyle headerStyle =
                createDictionaryHeaderStyle(
                        workbook
                );

        CellStyle bodyStyle =
                createDictionaryBodyStyle(
                        workbook
                );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                dictionarySheet.createRow(0);

        for (
                int i = 0;
                i < DATA_DICTIONARY_HEADERS.length;
                i++
        ) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    DATA_DICTIONARY_HEADERS[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Dictionary data
        // =========================================================

        String[][] dictionaryData = {

                {
                        "Transaction Type *",
                        "transaction_type",
                        "Yes",
                        "Transaction type. " +
                                "Enter a valid transaction type " +
                                "from the 'Transaction Types' sheet."
                },

                {
                        "Category Name *",
                        "user_category_id",
                        "Yes",
                        "Category associated with the " +
                                "subcategory. Enter a valid category " +
                                "from the 'Categories' sheet."
                },

                {
                        "Subcategory Name *",
                        "subcategory_name",
                        "Yes",
                        "Name of the subcategory."
                },

                {
                        "Subcategory Description *",
                        "subcategory_description",
                        "Yes",
                        "Description of the subcategory."
                }
        };

        for (
                int rowIndex = 0;
                rowIndex < dictionaryData.length;
                rowIndex++
        ) {

            Row row =
                    dictionarySheet.createRow(
                            rowIndex + 1
                    );

            row.setHeightInPoints(55);

            for (
                    int columnIndex = 0;
                    columnIndex < dictionaryData[rowIndex].length;
                    columnIndex++
            ) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellValue(
                        dictionaryData[rowIndex][columnIndex]
                );

                cell.setCellStyle(bodyStyle);
            }
        }

        // =========================================================
        // Formatting
        // =========================================================

        dictionarySheet.createFreezePane(0, 1);

        dictionarySheet.setColumnWidth(
                0,
                35 * 256
        );

        dictionarySheet.setColumnWidth(
                1,
                40 * 256
        );

        dictionarySheet.setColumnWidth(
                2,
                15 * 256
        );

        dictionarySheet.setColumnWidth(
                3,
                90 * 256
        );

        log.debug(
                "Subcategory Data Dictionary sheet created."
        );
    }

    private void createFinancialTransactionDataDictionarySheet(
            XSSFWorkbook workbook
    ) {

        XSSFSheet dictionarySheet =
                workbook.createSheet(
                        DATA_DICTIONARY_SHEET
                );

        CellStyle headerStyle =
                createDictionaryHeaderStyle(
                        workbook
                );

        CellStyle bodyStyle =
                createDictionaryBodyStyle(
                        workbook
                );

        // =========================================================
        // Header
        // =========================================================

        Row headerRow =
                dictionarySheet.createRow(0);

        for (
                int i = 0;
                i < FINANCIAL_TRANSACTIONS_HEADERS.length;
                i++
        ) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    FINANCIAL_TRANSACTIONS_HEADERS[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

        headerRow.setHeightInPoints(25);

        // =========================================================
        // Dictionary data
        // =========================================================

        String[][] dictionaryData = {

                {
                        "Transaction Date *",
                        "transaction_at",
                        "Yes",
                        "Transaction date. Enter a valid transaction date in one of the following formats: yyyy-MM-dd, MM/dd/yyyy, or dd/MM/yyyy."
                },

                {
                        "Amount *",
                        "amount",
                        "Yes",
                        "Transaction amount. Enter a valid amount. Both positive and negative values are allowed."
                },

                {
                        "Transaction Type *",
                        "transaction_type",
                        "Yes",
                        "Transaction type. Enter a valid transaction type from the 'Transaction Types' sheet."
                },

                {
                        "Category Name *",
                        "user_category_id",
                        "Yes",
                        "Category associated with the transaction. Enter a valid category from the 'Categories' sheet."
                },

                {
                        "Subcategory Name *",
                        "user_subcategory_id",
                        "Yes",
                        "Subcategory associated with the transaction. Enter a valid subcategory from the 'Subcategories' sheet that belongs to the selected category."
                },

                {
                        "Location",
                        "location",
                        "No",
                        "Location where the financial transaction occurred. Enter a valid Location value from the 'Locations' sheet. Use the value from the 'Location' column; the 'Description' column is provided for reference only."
                },

                {
                        "Remarks",
                        "remarks",
                        "No",
                        "Additional remarks or notes about the financial transaction."
                }
        };

        for (
                int rowIndex = 0;
                rowIndex < dictionaryData.length;
                rowIndex++
        ) {

            Row row =
                    dictionarySheet.createRow(
                            rowIndex + 1
                    );

            row.setHeightInPoints(55);

            for (
                    int columnIndex = 0;
                    columnIndex < dictionaryData[rowIndex].length;
                    columnIndex++
            ) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellValue(
                        dictionaryData[rowIndex][columnIndex]
                );

                cell.setCellStyle(bodyStyle);
            }
        }

        // =========================================================
        // Formatting
        // =========================================================

        dictionarySheet.createFreezePane(0, 1);

        dictionarySheet.setColumnWidth(
                0,
                35 * 256
        );

        dictionarySheet.setColumnWidth(
                1,
                40 * 256
        );

        dictionarySheet.setColumnWidth(
                2,
                15 * 256
        );

        dictionarySheet.setColumnWidth(
                3,
                90 * 256
        );

        log.debug(
                "Financial Transactions Data Dictionary sheet created."
        );
    }
}