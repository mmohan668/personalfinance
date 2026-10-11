package com.pf.common.service.generic;

import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.entity.userManagement.User;
import com.pf.common.enums.AppEnums;
import com.pf.common.repository.setting.SystemConfigRepository;
import com.pf.common.util.ExcelUtils;
import com.pf.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTemplateService {

    @Value("${export.base-folder}")
    private String exportPath;
    private final SystemConfigRepository systemConfigRepository;

    public static final String DATA = "data";

    public void createExcel(List<GridColumnDto> columns, List<?> data, User user, String gridName, String exportFileName) throws IOException {
        List<GridColumnDto> exportColumns = getExportEligibleColumns(columns);
        if (exportColumns.isEmpty()) {
            log.warn("All columns are non-exportable for the grid {} for the user {}", gridName, user);
            return;
        }
        Path basePath = Paths.get(exportPath).toAbsolutePath().normalize();
        Path outputPath = basePath.resolve(FileUtils.safePathSegment(user.getUsername()))
                .resolve(FileUtils.safePathSegment(gridName))
                .resolve(FileUtils.safeFileName(FileUtils.addTimestamp(exportFileName)))
                .normalize();
        // Ensure the final path stays inside the configured export directory.
        if (!outputPath.startsWith(basePath)) {
            throw new IOException("Invalid export file path");
        }
        Files.createDirectories(outputPath.getParent());
        try (ByteArrayInputStream inputStream =
                     createWorkBook(exportColumns, data, gridName, user)) {
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Excel file created at {}", outputPath);
    }

    public ByteArrayInputStream createWorkBook(List<GridColumnDto> columns, List<?> data, String gridName, User user) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(DATA);
            createHeader(workbook, sheet, columns);
            createBody(workbook, sheet, columns, data, user);
            workbook.setActiveSheet(0);
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Error creating Excel for grid name ={}", gridName, e);
            throw new IllegalStateException("Unable to create Excel for grid name for: " + gridName, e);
        }
    }

    private List<GridColumnDto> getExportEligibleColumns(List<GridColumnDto> gridColumns) {
        return gridColumns.stream()
                .filter(c -> /*c.getExportable() != null
                        && c.getExportable().equals(true)
                        &&*/ c.getVisible() != null
                        && c.getVisible().equals(true))
                .sorted(Comparator.comparing(GridColumnDto::getVisibleIndex))
                .toList();
    }

    private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet, List<GridColumnDto> columns) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = ExcelUtils.createHeaderStyle(workbook);
        int index = 0;
        for (GridColumnDto column : columns) {
            Cell cell = headerRow.createCell(index);
            cell.setCellValue(column.getHeader());
            cell.setCellStyle(headerStyle);
            ExcelUtils.setMaxWidth(index, sheet);
            index++;
        }
        headerRow.setHeightInPoints(25);
        sheet.createFreezePane(0, 1);
    }

    private void createBody(
            XSSFWorkbook workbook,
            XSSFSheet sheet,
            List<GridColumnDto> columns,
            List<?> data,
            User user) {
        SystemConfig systemConfig = systemConfigRepository.findByConfigNameAndUserId(AppEnums.SystemConfigName.CURRENCY.getValue(), user.getId());
        String dateFormat = systemConfig.getConfigValue().getReferenceCode3();
        String excelDateFormat = dateFormat.replace("/", "\\/");
        String currency = systemConfig.getConfigValue().getReferenceCode2();
        int rowIndex = 1;
        for (Object item : data) {
            Row row = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            for (GridColumnDto column : columns) {
                Cell cell = row.createCell(columnIndex++);
                try {
                    Object value = getPropertyValue(item, column.getField());
                    switch (value) {
                        case null -> cell.setBlank();
                        case Number number -> cell.setCellValue(number.doubleValue());
                        case Boolean bool -> cell.setCellValue(bool);
                        case LocalDate date -> {
                            cell.setCellValue(date);
                            CellStyle dateStyle = workbook.createCellStyle();
                            dateStyle.setDataFormat(
                                    workbook.getCreationHelper()
                                            .createDataFormat()
                                            .getFormat(excelDateFormat));
                            cell.setCellStyle(dateStyle);
                        }
                        case LocalDateTime date -> {
                            cell.setCellValue(date);
                            CellStyle dateStyle = workbook.createCellStyle();
                            dateStyle.setDataFormat(
                                    workbook.getCreationHelper()
                                            .createDataFormat()
                                            .getFormat(excelDateFormat + " HH:mm:ss"));
                            cell.setCellStyle(dateStyle);
                        }
                        default -> cell.setCellValue(value.toString());
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Unable to read field '" + column.getField() + "'", e);
                }
                ExcelUtils.setMaxWidth(columnIndex - 1, sheet);
            }
        }
    }

    private Object getPropertyValue(Object item, String fieldName)
            throws ReflectiveOperationException {
        if (item == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        Class<?> clazz = item.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(item);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
