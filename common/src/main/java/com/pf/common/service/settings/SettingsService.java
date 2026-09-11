package com.pf.common.service.settings;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.mapper.settings.ReferenceObjectMapper;
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
    private final ReferenceObjectRepository referenceObjectRepository;

    public GridResult fetchReferenceObjectGridData(SearchCriteria searchCriteria) {
        try {
            log.debug("fetchReferenceObjectGridData: {}", searchCriteria);
            long totalRecords = criteriaService.getCountBySearchCriteria(ReferenceObject.class, searchCriteria);
            List<ReferenceObjectDto> recordDetails = referenceObjectMapper.toDtoList(criteriaService.getDataBySearchCriteria(ReferenceObject.class, searchCriteria));
            log.debug("fetchReferenceObjectGridData: totalRecords: {}", totalRecords);
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
                    .message("Reference Object Name save failed.")
                    .build();
        }
    }
}
