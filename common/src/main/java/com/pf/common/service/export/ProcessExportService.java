package com.pf.common.service.export;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.userManagement.User;
import com.pf.common.enums.GridNames;
import com.pf.common.logging.UserLogContext;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.bulkUpload.BulkUploadsService;
import com.pf.common.service.categoryManagement.CategoryManagementService;
import com.pf.common.service.financialTransaction.FinancialTransactionService;
import com.pf.common.service.financialTransaction.RecurringTransactionService;
import com.pf.common.service.gp.GridService;
import com.pf.common.service.settings.SettingsService;
import com.pf.common.service.userManagement.UserService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessExportService {
    private final FinancialTransactionService financialTransactionService;
    private final CategoryManagementService categoryManagementService;
    private final BulkUploadsService bulkUploadsService;
    private final SettingsService settingsService;
    private final RecurringTransactionService recurringTransactionService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GridService gridService;
    private final ExportTemplateService exportTemplateService;

    @Transactional
    public void processServerSideExport(ExportRequest exportRequest) throws IOException {
        User user = userRepository.findById(exportRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found for user id " + exportRequest.getUserId()
                ));
        try (UserLogContext ignored = UserLogContext.open(user.getId(), user.getUsername())) {
            processServerSideExport(exportRequest, user);
        }
    }

    private void processServerSideExport(ExportRequest exportRequest, User user) throws IOException {
        log.info(
                "Processing Server Side Export Request for the grid {} for user id {}",
                exportRequest.getSearchCriteria().getGridName(),
                exportRequest.getUserId()
        );
        List<GridColumnDto> gridColumns = gridService.fetchGridColumns(
                exportRequest.getSearchCriteria().getGridName(),
                exportRequest.getUserId()
        );
        if (gridColumns == null || gridColumns.isEmpty()) {
            log.error("No grid columns found for user id {}", exportRequest.getUserId());
            return;
        }
        log.info("Grid columns found for user id {}", exportRequest.getUserId());
        List<?> gridData = fetchGridDataAsUser(exportRequest.getSearchCriteria(), user);
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

    private List<?> fetchGridDataAsUser(SearchCriteria searchCriteria, User user) {
        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContext exportContext = SecurityContextHolder.createEmptyContext();
        exportContext.setAuthentication(createExportAuthentication(user));
        SecurityContextHolder.setContext(exportContext);
        try {
            return fetchGridData(searchCriteria);
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    private JwtAuthenticationToken createExportAuthentication(User user) {
        Long adminUserId = Boolean.TRUE.equals(user.getIsAdminUser())
                ? user.getId()
                : user.getAdminUser() == null ? null : user.getAdminUser().getId();
        if (adminUserId == null) {
            throw new IllegalStateException(
                    "Cannot determine admin user for export user id " + user.getId()
            );
        }
        Jwt jwt = Jwt.withTokenValue("server-side-export")
                .header("alg", "none")
                .subject(user.getId().toString())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("adminUserId", adminUserId)
                .claim("admin", Boolean.TRUE.equals(user.getIsAdminUser()))
                .build();
        return new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES);
    }

    private List<?> fetchGridData(SearchCriteria searchCriteria) {
        try {
            GridNames gridName = GridNames.valueOf(searchCriteria.getGridName());
            return switch (gridName) {
                case ALL_TRANSACTIONS_GRID, INCOMES_GRID, EXPENSES_GRID, TRANSFERS_GRID, INVESTMENTS_GRID ->
                        financialTransactionService.fetchFinancialTransactionGridData(searchCriteria).getRecordDetails();
                case CATEGORIES_GRID ->
                        categoryManagementService.fetchCategoriesGridData(searchCriteria).getRecordDetails();
                case SUBCATEGORIES_GRID ->
                        categoryManagementService.fetchSubcategoriesGridData(searchCriteria).getRecordDetails();
                case REFERENCE_OBJECTS_GRID ->
                        settingsService.fetchReferenceObjectGridData(searchCriteria).getRecordDetails();
                case REFERENCE_VALUES_GRID ->
                        settingsService.fetchReferenceValueGridData(searchCriteria).getRecordDetails();
                case SYSTEM_CONFIG -> settingsService.fetchSystemConfigGridData(searchCriteria).getRecordDetails();
                case RECURRING_TRANSACTIONS_GRID -> recurringTransactionService.fetchGridData(searchCriteria).getRecordDetails();
                case BULK_UPLOADS_STATUS ->
                        bulkUploadsService.fetchBulkUploadStatusGridData(searchCriteria).getRecordDetails();
                case USERS_GRID -> userService.fetchUsersGridData(searchCriteria).getRecordDetails();
            };
        } catch (RuntimeException e) {
            log.error(
                    "Error getting grid data for grid {}",
                    searchCriteria.getGridName(),
                    e
            );
            throw e;
        }
    }
}
