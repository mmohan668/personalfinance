package com.pf.common.service.export;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.userManagement.User;
import com.pf.common.enums.GridNames;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.categoryManagement.CategoryManagementService;
import com.pf.common.service.financialTransaction.FinancialTransactionService;
import com.pf.common.service.gp.GridService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessExportService {
    private final FinancialTransactionService financialTransactionService;
    private final CategoryManagementService categoryManagementService;
    private final UserRepository userRepository;
    private final GridService gridService;
    private final ExportTemplateService exportTemplateService;

    @Transactional
    public void processServerSideExport(ExportRequest exportRequest) throws IOException {
        log.info(
                "Processing Server Side Export Request for the grid {} for user id {}",
                exportRequest.getSearchCriteria().getGridName(),
                exportRequest.getUserId()
        );
        User user = userRepository.findById(exportRequest.getUserId()).orElse(null);
        if (user == null) {
            log.error("User not found for user id {}", exportRequest.getUserId());
            return;
        }
        List<GridColumnDto> gridColumns = gridService.fetchGridColumns(
                exportRequest.getSearchCriteria().getGridName(),
                exportRequest.getUserId()
        );
        if (gridColumns == null || gridColumns.isEmpty()) {
            log.error("No grid columns found for user id {}", exportRequest.getUserId());
            return;
        }
        log.info("Grid columns found for user id {}", exportRequest.getUserId());
        List<?> gridData = fetchGridData(exportRequest.getSearchCriteria());
        if (gridData == null || gridData.isEmpty()) {
            log.warn(
                    "Empty gridData for grid {} for user id {}",
                    exportRequest.getSearchCriteria().getGridName(),
                    exportRequest.getUserId()
            );
            return;
        }
        log.info("Grid data fetched successfully. Creating Excel");
        exportTemplateService.createExcel(
                gridColumns,
                gridData,
                user,
                exportRequest.getSearchCriteria().getGridName(),
                exportRequest.getExportFileName()
        );
    }

    private List<?> fetchGridData(SearchCriteria searchCriteria) {
        GridNames gridName = GridNames.valueOf(searchCriteria.getGridName());
        try {
            return switch (gridName) {
                case ALL_TRANSACTIONS_GRID ->
                        financialTransactionService.fetchFinancialTransactionGridData(searchCriteria).getRecordDetails();
                case CATEGORIES_GRID ->
                        categoryManagementService.fetchCategoriesGridData(searchCriteria).getRecordDetails();
                case SUBCATEGORIES_GRID ->
                        categoryManagementService.fetchSubcategoriesGridData(searchCriteria).getRecordDetails();
                default -> new ArrayList<>();
            };
        } catch (Exception e) {
            log.error("Error getting grid data", e);
            return new ArrayList<>();
        }
    }
}
