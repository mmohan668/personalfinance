package com.pf.common.service.settings;

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

    public ApiResponse saveReferenceObject(ReferenceObjectDto referenceObjectDto) {
        try {
            log.debug("saveReferenceObject: {}", referenceObjectDto);

            ReferenceObject referenceObject =
                    referenceObjectMapper.toEntity(referenceObjectDto);

            referenceObject.setRefObjName(referenceObject.getRefObjName().trim());

            if (referenceObject.getId() == null) {
                //Create
                if (referenceObjectRepository
                        .getCountByReferenceObjectName(referenceObject.getRefObjName()) > 0) {

                    log.error("addReferenceObject: reference object already exists");

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference object name already exists")
                            .build();
                }
                referenceObject.setCreatedBy("testuser");
            } else {
                //Update
                if (referenceObjectRepository
                        .getCountByReferenceObjectNameIdNotEquals(
                                referenceObject.getRefObjName(),
                                referenceObject.getId()) > 0
                ) {
                    log.error("updateReferenceObject: reference object already exists");

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference object name already exists")
                            .build();
                }
                referenceObject.setUpdatedBy("testuser");
            }
            referenceObjectRepository.save(referenceObject);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference Object Name saved successfully.")
                    .build();
        } catch (Exception e) {
            log.error(
                    "Error occurred while saving reference object: {}",
                    referenceObjectDto,
                    e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Reference Object Name save failed. Please contact system administrator.")
                    .build();
        }
    }

    public ApiResponse deleteReferenceObject(List<Long> ids) {
        try {
            log.debug("deleteReferenceObject: {}", ids);
            referenceObjectRepository.deleteAllById(ids);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference object deleted successfully.")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while deleting reference object: {}", ids, e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Reference object delete failed. Please contact system administrator.")
                    .build();
        }
    }
}
