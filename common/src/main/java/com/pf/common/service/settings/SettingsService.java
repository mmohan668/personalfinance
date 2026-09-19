package com.pf.common.service.settings;

import com.google.common.collect.Lists;
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
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pf.common.constants.CommonConstants.*;
import static com.pf.common.constants.FieldConstants.CREATED_BY;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.FilterOperator.IN;
import static com.pf.common.enums.SortOrder.ASC;
import static com.pf.common.constants.EntityConstants.REFERENCE_OBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService extends BaseService {
    private final ReferenceObjectMapper referenceObjectMapper;
    private final ReferenceValueMapper referenceValueMapper;
    private final ReferenceObjectRepository referenceObjectRepository;
    private final ReferenceValueRepository referenceValueRepository;

    public GridResult fetchReferenceObjectGridData(SearchCriteria searchCriteria) {
        log.debug("fetchReferenceObjectGridData: {}", searchCriteria);
        GridFilter gridFilter = GridFilter.builder()
                .field(CREATED_BY)
                .operator(IN.getValue())
                .values(List.of(SYSTEM, TEST_USER))
                .build();
        searchCriteria.getFilterList().add(gridFilter);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        long totalRecords = getCountBySearchCriteria(ReferenceObject.class, searchCriteria);
        List<ReferenceObjectDto> recordDetails = referenceObjectMapper.toDtoList(getDataBySearchCriteria(ReferenceObject.class, searchCriteria));
        log.debug("fetchReferenceObjectGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    public GridResult fetchReferenceValueGridData(SearchCriteria searchCriteria) {
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
        long totalRecords = getCountBySearchCriteria(ReferenceValue.class, searchCriteria);
        List<ReferenceValueDto> recordDetails = referenceValueMapper.toDtoList(getDataBySearchCriteria(ReferenceValue.class, searchCriteria));
        log.debug("fetchReferenceValuesGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveReferenceValue(ReferenceValueDto referenceValueDto) {
        log.debug("saveReferenceValue: {}", referenceValueDto);
        ReferenceObject referenceObject = referenceObjectRepository.findById(referenceValueDto.getRefObjNameId()).orElse(null);
        if (referenceObject == null) {
            log.warn(
                    "saveReferenceValue: Reference object not found. refObjNameId = {}",
                    referenceValueDto.getRefObjNameId()
            );
            return failure("Reference object not found.");
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
                return failure("Reference code already exists for the selected reference object.");
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
                return failure("Reference value not found.");
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
                return failure("Reference code already exists for the selected reference object.");
            }
            referenceValue.setReferenceObject(referenceObject);
            referenceValue.setReferenceCode(referenceValueDto.getReferenceCode());
            referenceValue.setReferenceCodeDescription(referenceValueDto.getReferenceCodeDescription());
            referenceValue.setUpdatedBy(TEST_USER);
        }
        referenceValueRepository.save(referenceValue);
        return success("Reference value saved successfully.");
    }

    @Transactional
    public ApiResponse deleteReferenceValue(List<Long> ids) {
        log.debug("deleteReferenceValue: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return failure("No reference values selected for deletion.");
        }
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            referenceValueRepository.deleteAllByIdInBatch(chunk);
        }
        return success("Reference value(s) deleted successfully.");
    }

    public List<SelectItem> fetchCategoryTypes() {
        return referenceObjectRepository.fetchCategoryTypes();
    }
}
