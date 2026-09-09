package com.pf.common.service.gp;

import com.pf.common.dto.gp.*;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.gp.GridPersonalization;
import com.pf.common.mapper.gp.GridColumnMapper;
import com.pf.common.mapper.gp.GridPersonalizationMapper;
import com.pf.common.repository.gp.GridColumnRepository;
import com.pf.common.repository.gp.GridNameRepository;
import com.pf.common.repository.gp.GridPersonalizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridService {

    private final GridColumnRepository gridColumnRepository;
    private final GridColumnMapper gridColumnMapper;
    private final GridPersonalizationRepository gridPersonalizationRepository;
    private final GridNameRepository gridNameRepository;
    private final GridPersonalizationMapper gridPersonalizationMapper;
    private final ObjectMapper objectMapper;

    public List<GridColumnDto> fetchGridColumns(String gridName, Long userId) {
        try {
            log.debug(
                    "Fetching grid columns for gridName: {}, userId: {}",
                    gridName,
                    userId
            );

            Long gridId = gridNameRepository.findIdByName(gridName);

            if (gridId == null) {
                log.warn(
                        "Grid configuration not found for gridName: {}, userId: {}",
                        gridName,
                        userId
                );
                return Collections.emptyList();
            }

            GridPersonalization gridPersonalization =
                    gridPersonalizationRepository
                            .findByGridName_NameAndUserId(gridName, userId)
                            .orElse(null);

            if (gridPersonalization != null) {
                log.debug(
                        "User-specific grid personalization found for gridName: {}, userId: {}",
                        gridName,
                        userId
                );

                String gridColumnJson = gridPersonalization.getGridColumnJson();

                List<GridColumnDto> personalizedColumns =
                        objectMapper.readValue(
                                gridColumnJson,
                                new TypeReference<>() {
                                }
                        );

                return Collections.unmodifiableList(personalizedColumns);
            }

            log.debug(
                    "No user-specific grid personalization found for gridName: {}, userId: {}. " +
                            "Fetching default grid columns.",
                    gridName,
                    userId
            );

            List<GridColumnDto> defaultColumns =
                    gridColumnMapper.toDTOList(
                            gridColumnRepository
                                    .findByGridName_NameOrderByVisibleIndex(gridName)
                    );

            if (defaultColumns.isEmpty()) {
                log.debug(
                        "No default grid columns found for gridName: {}",
                        gridName
                );
                return Collections.emptyList();
            }

            log.debug(
                    "Successfully fetched {} default grid columns for gridName: {}",
                    defaultColumns.size(),
                    gridName
            );

            return Collections.unmodifiableList(defaultColumns);

        } catch (DataAccessException e) {
            log.error(
                    "Database error while fetching grid columns for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );
            throw e;

        } catch (Exception e) {
            log.error(
                    "Unexpected error while fetching grid columns for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );
            throw e;
        }
    }

    @Transactional
    public ApiResponse saveGridSettings(
            GridPersonalizationDto gridPersonalizationDto
    ) {
        String gridName = gridPersonalizationDto.getGridName();
        Long userId = gridPersonalizationDto.getUserId();

        try {
            log.info(
                    "Saving grid personalization for gridName: {}, userId: {}",
                    gridName,
                    userId
            );

            Long gridId = gridNameRepository.findIdByName(gridName);

            if (gridId == null) {
                log.warn(
                        "Cannot save grid personalization. Grid configuration not found " +
                                "for gridName: {}, userId: {}",
                        gridName,
                        userId
                );

                return ApiResponse.builder()
                        .success(false)
                        .message("Grid setting save failed: Grid not found")
                        .build();
            }

            GridPersonalization gridPersonalization =
                    gridPersonalizationRepository
                            .findByGridName_NameAndUserId(gridName, userId)
                            .orElse(null);

            if (gridPersonalization == null) {
                log.debug(
                        "No existing grid personalization found for gridName: {}, userId: {}. " +
                                "Creating new personalization.",
                        gridName,
                        userId
                );

                gridPersonalizationDto.setGridNameId(gridId);

                gridPersonalization =
                        gridPersonalizationMapper.toEntity(gridPersonalizationDto);
            } else {
                log.debug(
                        "Existing grid personalization found for gridName: {}, userId: {}. " +
                                "Updating personalization.",
                        gridName,
                        userId
                );

                gridPersonalization.setGridColumnJson(
                        gridPersonalizationDto.getGridColumnJson()
                );
            }

            gridPersonalizationRepository.save(gridPersonalization);

            log.info(
                    "Grid personalization saved successfully for gridName: {}, userId: {}",
                    gridName,
                    userId
            );

            return ApiResponse.builder()
                    .success(true)
                    .message("Grid setting saved successfully")
                    .build();

        } catch (DataIntegrityViolationException e) {
            log.error(
                    "Data integrity violation while saving grid personalization " +
                            "for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );

            return ApiResponse.builder()
                    .success(false)
                    .message("Grid setting save failed: Invalid or duplicate data")
                    .build();

        } catch (DataAccessException e) {
            log.error(
                    "Database error while saving grid personalization " +
                            "for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );

            return ApiResponse.builder()
                    .success(false)
                    .message("Grid setting save failed: Database error")
                    .build();

        } catch (Exception e) {
            log.error(
                    "Unexpected error while saving grid personalization " +
                            "for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );

            return ApiResponse.builder()
                    .success(false)
                    .message("Grid setting save failed")
                    .build();
        }
    }

    @Transactional
    public ApiResponse resetGridPersonalization(
            GridPersonalizationDto gridPersonalizationDto
    ) {
        String gridName = gridPersonalizationDto.getGridName();
        Long userId = gridPersonalizationDto.getUserId();

        try {
            log.info(
                    "Resetting grid personalization for gridName: {}, userId: {}",
                    gridName,
                    userId
            );

            int deleteCount =
                    gridPersonalizationRepository
                            .deleteByGridName_NameAndUserId(gridName, userId);

            if (deleteCount == 0) {
                log.info(
                        "No grid personalization found to reset for gridName: {}, userId: {}",
                        gridName,
                        userId
                );

                return ApiResponse.builder()
                        .success(true)
                        .message("No personalized grid setting found to reset")
                        .build();
            }

            log.info(
                    "Grid personalization reset successfully for gridName: {}, userId: {}. " +
                            "Deleted records: {}",
                    gridName,
                    userId,
                    deleteCount
            );

            return ApiResponse.builder()
                    .success(true)
                    .message("Grid setting reset successfully")
                    .build();

        } catch (DataAccessException e) {
            log.error(
                    "Database error while resetting grid personalization " +
                            "for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );

            return ApiResponse.builder()
                    .success(false)
                    .message("Grid setting reset failed: Database error")
                    .build();

        } catch (Exception e) {
            log.error(
                    "Unexpected error while resetting grid personalization " +
                            "for gridName: {}, userId: {}",
                    gridName,
                    userId,
                    e
            );

            return ApiResponse.builder()
                    .success(false)
                    .message("Grid setting reset failed")
                    .build();
        }
    }
}