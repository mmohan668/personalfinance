package com.pf.common.service.gp;

import com.pf.common.dto.gp.*;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.gp.GridPersonalization;
import com.pf.common.mapper.gp.GridColumnMapper;
import com.pf.common.mapper.gp.GridPersonalizationMapper;
import com.pf.common.repository.gp.GridColumnRepository;
import com.pf.common.repository.gp.GridNameRepository;
import com.pf.common.repository.gp.GridPersonalizationRepository;
import com.pf.common.service.generic.BaseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GridService extends BaseService {

    private final GridColumnRepository gridColumnRepository;
    private final GridColumnMapper gridColumnMapper;
    private final GridPersonalizationRepository gridPersonalizationRepository;
    private final GridNameRepository gridNameRepository;
    private final GridPersonalizationMapper gridPersonalizationMapper;
    private final ObjectMapper objectMapper;

    public List<GridColumnDto> fetchGridColumns(String gridName, Long userId) {
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
    }

    @Transactional
    public ApiResponse saveGridSettings(
            GridPersonalizationDto gridPersonalizationDto
    ) {
        String gridName = gridPersonalizationDto.getGridName();
        Long userId = gridPersonalizationDto.getUserId();
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
            return failure("Grid setting save failed: Grid not found");
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
        return success("Grid setting saved successfully");
    }

    @Transactional
    public ApiResponse resetGridPersonalization(
            GridPersonalizationDto gridPersonalizationDto
    ) {
        String gridName = gridPersonalizationDto.getGridName();
        Long userId = gridPersonalizationDto.getUserId();
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
            return failure("No personalized grid setting found to reset");
        }
        log.info(
                "Grid personalization reset successfully for gridName: {}, userId: {}. " +
                        "Deleted records: {}",
                gridName,
                userId,
                deleteCount
        );
        return success("Grid setting reset successfully");
    }
}