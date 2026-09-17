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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.pf.common.constants.CommonConstants.SYSTEM;
import static com.pf.common.constants.CommonConstants.TEST_USER;
import static com.pf.common.constants.FieldConstants.CREATED_BY;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.FilterOperator.IN;
import static com.pf.common.enums.SortOrder.ASC;
import static com.pf.common.constants.EntityConstants.REFERENCE_OBJECT;

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
                    .field(CREATED_BY)
                    .operator(IN.getValue())
                    .values(List.of(SYSTEM, TEST_USER))
                    .build();
            searchCriteria.getFilterList().add(gridFilter);
            searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
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
                    .field(CREATED_BY)
                    .operator(IN.getValue())
                    .values(List.of(SYSTEM, TEST_USER))
                    .build();
            searchCriteria.getFilterList().add(gridFilter);
            searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
            searchCriteria.setFetchPaths(List.of(REFERENCE_OBJECT));
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

    @Transactional
    public ApiResponse saveReferenceValue(ReferenceValueDto referenceValueDto) {
        try {
            log.debug("saveReferenceValue: {}", referenceValueDto);

            ReferenceObject referenceObject = referenceObjectRepository.findById(referenceValueDto.getRefObjNameId()).orElse(null);
            if (referenceObject == null) {
                log.warn(
                        "saveReferenceValue: Reference object not found. refObjNameId = {}",
                        referenceValueDto.getRefObjNameId()
                );

                return ApiResponse.builder()
                        .success(false)
                        .message("Reference object not found.")
                        .build();
            }
            ReferenceValue referenceValue;
            if (referenceValueDto.getId() == null) {
                //Create
                referenceValue =
                        referenceValueMapper.toEntity(referenceValueDto);

                referenceValue.setReferenceObject(referenceObject);

                if (referenceValueRepository
                        .countByReferenceObjectIdAndReferenceCode(
                                referenceObject.getId(),
                                referenceValue.getReferenceCode()
                        ) > 0
                ) {

                    log.warn(
                            "Reference code already exists for the selected reference object. refObjName = {}, referenceCode = {}",
                            referenceObject.getRefObjName(),
                            referenceValue.getReferenceCode()
                    );

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference code already exists for the selected reference object.")
                            .build();
                }
                referenceValue.setCreatedBy(TEST_USER);
            } else {
                //Update
                referenceValue = referenceValueRepository.findById(referenceValueDto.getId()).orElse(null);
                if (referenceValue == null) {
                    log.warn(
                            "saveReferenceValue: Reference value not found. referenceValueId = {}",
                            referenceValueDto.getId()
                    );

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference value not found.")
                            .build();
                }

                if (referenceValueRepository
                        .countByReferenceObjectIdAndReferenceCodeAndIdNot(
                                referenceObject.getId(),
                                referenceValueDto.getReferenceCode(),
                                referenceValue.getId()) > 0
                ) {
                    log.warn(
                            "Reference code already exists for the selected reference object. referenceCode = {}, refObjName {}",
                            referenceValueDto.getReferenceCode(),
                            referenceObject.getRefObjName()
                    );

                    return ApiResponse.builder()
                            .success(false)
                            .message("Reference code already exists for the selected reference object.")
                            .build();
                }

                referenceValue.setReferenceObject(referenceObject);
                referenceValue.setReferenceCode(referenceValueDto.getReferenceCode());
                referenceValue.setReferenceCodeDescription(referenceValueDto.getReferenceCodeDescription());
                referenceValue.setUpdatedBy(TEST_USER);
            }
            referenceValueRepository.save(referenceValue);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference value saved successfully.")
                    .build();
        } catch (Exception e) {
            log.error(
                    "Error occurred while saving reference value: {}",
                    referenceValueDto,
                    e
            );
            return ApiResponse.builder()
                    .success(false)
                    .message("Failed to save reference value. Please contact the system administrator.")
                    .build();
        }
    }

    @Transactional
    public ApiResponse deleteReferenceValue(List<Long> ids) {
        try {
            log.debug("deleteReferenceValue: {}", ids);

            if (ids == null || ids.isEmpty()) {
                return ApiResponse.builder()
                        .success(false)
                        .message("No reference values selected for deletion.")
                        .build();
            }

            referenceValueRepository.deleteAllById(ids);
            return ApiResponse.builder()
                    .success(true)
                    .message("Reference value(s) deleted successfully.")
                    .build();
        } catch (Exception e) {
            log.error(
                    "Error occurred while deleting reference value(s): {}",
                    ids,
                    e
            );
            return ApiResponse.builder()
                    .success(false)
                    .message("Failed to delete reference value(s). Please contact the system administrator.")
                    .build();
        }
    }

    public List<SelectItem> fetchCategoryTypes() {
        return referenceObjectRepository.fetchCategoryTypes();
    }
}
