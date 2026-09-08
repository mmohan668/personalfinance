package com.pf.common.service.gp;

import org.springframework.boot.json.JsonParseException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.pf.common.dto.gp.*;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.gp.GridPersonalization;
import com.pf.common.mapper.gp.GridColumnMapper;
import com.pf.common.mapper.gp.GridPersonalizationMapper;
import com.pf.common.repository.GridColumnRepository;
import com.pf.common.repository.GridNameRepository;
import com.pf.common.repository.GridPersonalizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
            List<GridColumnDto> list;
            Long gridId = gridNameRepository.findIdByName(gridName);
            if (gridId == null) {
                log.warn("GridName not found for: {}", gridName);
                return Collections.emptyList();
            }

            GridPersonalization gridPersonalization =
                    gridPersonalizationRepository.findByGridName_IdAndUserId(gridId, userId).orElse(null);

            if (gridPersonalization != null) {
                String gridColumnJson = gridPersonalization.getGridColumnJson();
                list = objectMapper.readValue(gridColumnJson, new TypeReference<>() {
                });
                return Collections.unmodifiableList(list);
            }

            log.info("Fetching grid columns for grid name {}", gridName);
            list = gridColumnMapper.toDTOList(gridColumnRepository.findByGridName_NameOrderByVisibleIndex(gridName));
            if (list.isEmpty()) {
                log.debug("No GridColumn entries found for gridName: {}", gridName);
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(list);
        } catch (DataAccessException dae) {
            log.error("Database error while fetching GridColumn entries for gridName: {}", gridName, dae);
            throw dae; // propagate or wrap in custom exception
        } catch (JsonParseException ex) {
            log.error("JsonProcessingException while fetching grid columns for gridName: {}", gridName, ex);
            throw new RuntimeException(ex);
        } catch (Exception e) {
            log.error("Unexpected error while fetching GridColumn entries for gridName: {}", gridName, e);
            throw e; // don’t silently swallow
        }
    }

    @Transactional
    public ApiResponse saveGridSettings(GridPersonalizationDto gridPersonalizationDto) {
        try {
            log.info("Saving GridPersonalization for gridPersonalizationDto: {}", gridPersonalizationDto);

            Long gridId = gridNameRepository.findIdByName(gridPersonalizationDto.getGridName());
            if (gridId == null) {
                log.error("GridName not found for: {}", gridPersonalizationDto.getGridName());
                return ApiResponse.builder()
                        .success(false)
                        .message("Grid Setting save failed: GridName not found")
                        .build();
            }

            GridPersonalization gridPersonalization = gridPersonalizationRepository.findByGridName_IdAndUserId(gridId, gridPersonalizationDto.getUserId()).orElse(null);
            if (gridPersonalization == null) {
                gridPersonalizationDto.setGridNameId(gridId);
                gridPersonalization = gridPersonalizationMapper.toEntity(gridPersonalizationDto);
            } else {
                gridPersonalization.setGridColumnJson(gridPersonalizationDto.getGridColumnJson());
            }

            gridPersonalizationRepository.save(gridPersonalization);

            log.info("Saved GridPersonalization for gridPersonalizationDto: {}", gridPersonalizationDto);
            return ApiResponse.builder()
                    .success(true)
                    .message("Grid Setting saved successfully")
                    .build();
        } catch (DataIntegrityViolationException e) {
            log.error("Constraint violation while saving Grid Settings for gridName: {}", gridPersonalizationDto.getGridName(), e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Grid Setting save failed: Duplicate or invalid data")
                    .build();
        } catch (Exception e) {
            log.error("Error while saving Grid Settings for gridName: {}", gridPersonalizationDto.getGridName(), e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Grid Setting save failed: Exception occurred")
                    .build();
        }
    }

}
