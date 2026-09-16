package com.pf.common.service.settings;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.dto.gp.GridFilter;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.mapper.settings.ReferenceObjectMapper;
import com.pf.common.mapper.settings.ReferenceValueMapper;
import com.pf.common.repository.setting.ReferenceObjectRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.service.criteria.GenericCriteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {
    private final GenericCriteriaService criteriaService;
    private final ReferenceObjectMapper referenceObjectMapper;
    private final ReferenceValueMapper referenceValueMapper;
    private final ReferenceObjectRepository referenceObjectRepository;
    private final ReferenceValueRepository referenceValueRepository;

    public GridResult fetchReferenceObjectGridData(SearchCriteria searchCriteria) {
        try {
            log.debug("fetchReferenceObjectGridData: {}", searchCriteria);
            GridFilter gridFilter = GridFilter.builder()
                    .field("createdBy")
                    .operator("in")
                    .values(List.of("SYSTEM", "testuser"))
                    .build();
            searchCriteria.getFilterList().add(gridFilter);
            searchCriteria.getSortList().add(new GridSort("id", "asc"));
            long totalRecords = criteriaService.getCountBySearchCriteria(ReferenceObject.class, searchCriteria);
            List<ReferenceObjectDto> recordDetails = referenceObjectMapper.toDtoList(criteriaService.getDataBySearchCriteria(ReferenceObject.class, searchCriteria));
            log.debug("fetchReferenceObjectGridData: totalRecords: {}", totalRecords);
            return GridResult.builder().totalRecords(totalRecords).recordDetails(recordDetails).build();
        } catch (Exception e) {
            log.error("Error occurred while fetching grid data for criteria: {}", searchCriteria, e);
            return GridResult.builder().totalRecords(0).recordDetails(Collections.emptyList()).build();
        }
    }

    public GridResult fetchReferenceValueGridData(SearchCriteria searchCriteria) {
        try {
            log.debug("fetchReferenceValuesGridData: {}", searchCriteria);
            GridFilter gridFilter = GridFilter.builder()
                    .field("createdBy")
                    .operator("in")
                    .values(List.of("SYSTEM", "testuser"))
                    .build();
            searchCriteria.getFilterList().add(gridFilter);
            searchCriteria.getSortList().add(new GridSort("id", "asc"));
            searchCriteria.setFetchPaths(List.of("referenceObject"));
            searchCriteria.setFIELD_MAPPINGS(ReferenceValueDto.FIELD_MAPPINGS);
            long totalRecords = criteriaService.getCountBySearchCriteria(ReferenceValue.class, searchCriteria);
            List<ReferenceValueDto> recordDetails = referenceValueMapper.toDtoList(criteriaService.getDataBySearchCriteria(ReferenceValue.class, searchCriteria));
            log.debug("fetchReferenceValuesGridData: totalRecords: {}", totalRecords);
            return GridResult.builder().totalRecords(totalRecords).recordDetails(recordDetails).build();
        } catch (Exception e) {
            log.error("Error occurred while fetching grid data for criteria: {}", searchCriteria, e);
            return GridResult.builder().totalRecords(0).recordDetails(Collections.emptyList()).build();
        }
    }

    public ApiResponse saveReferenceValue(ReferenceValueDto referenceValueDto) {
        try {
            log.debug("saveReferenceValue: {}", referenceValueDto);

            ReferenceValue referenceValue =
                    referenceValueMapper.toEntity(referenceValueDto);

            referenceValue.setReferenceCode(referenceValue.getReferenceCode().trim());
            referenceValue.setReferenceCodeDescription(referenceValue.getReferenceCodeDescription().trim());

            ReferenceObject referenceObject = referenceObjectRepository.findById(referenceValueDto.getRefObjNameId()).orElse(null);
            referenceValue.setReferenceObject(referenceObject);

            if (referenceValue.getId() == null) {
                //Create
                if (referenceValueRepository
                        .countByReferenceObjectIdAndReferenceCode(
                                referenceValue.getReferenceObject().getId(),
                                referenceValue.getReferenceCode()
                        ) > 0
                ) {

                    log.error("addReferenceValue: reference object name and value already exists");

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference object name and value combination already exists")
                            .build();
                }
                referenceValue.setCreatedBy("testuser");
            } else {
                //Update
                if (referenceValueRepository
                        .getCountByReferenceObjectNameIdNotEquals(
                                referenceValue.getReferenceObject().getId(),
                                referenceValue.getReferenceCode(),
                                referenceValue.getId()) > 0
                ) {
                    log.error("updateReferenceObject: reference value already exists");

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference value already exists")
                            .build();
                }
                referenceValue = referenceValueRepository.findById(referenceValue.getId()).orElse(null);
                if (referenceValue == null) {
                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference value not found")
                            .build();
                }
                referenceValue.setReferenceObject(referenceObject);
                referenceValue.setReferenceCodeDescription(referenceValueDto.getReferenceCodeDescription().trim());
                referenceValue.setReferenceCode(referenceValueDto.getReferenceCode().trim());
                referenceValue.setUpdatedBy("testuser");
            }
            referenceValueRepository.save(referenceValue);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference value saved successfully.")
                    .build();
        } catch (Exception e) {
            log.error(
                    "Error occurred while saving reference code: {}",
                    referenceValueDto,
                    e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Reference Code save failed. Please contact system administrator.")
                    .build();
        }
    }

    public ApiResponse deleteReferenceValue(List<Long> ids) {
        try {
            log.debug("deleteReferenceValue: {}", ids);
            referenceValueRepository.deleteAllById(ids);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference value(s) deleted successfully.")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while deleting reference value(s): {}", ids, e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Reference value(s) delete failed. Please contact system administrator.")
                    .build();
        }
    }

    public List<SelectItem> fetchCategoryTypes() {
        return referenceObjectRepository.fetchCategoryTypes();
    }
}
