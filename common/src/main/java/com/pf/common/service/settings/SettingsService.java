package com.pf.common.service.settings;

import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.mapper.settings.ReferenceObjectMapper;
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
}
