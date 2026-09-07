package com.pf.common.service.categoryManagement;

import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.mapper.categoryManagement.UserCategoryMapper;
import com.pf.common.service.criteria.GenericCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryManagementService {
    private final GenericCriteriaRepository criteriaRepository;
    private final UserCategoryMapper userCategoryMapper;

    public GridResult fetchCategoriesGridData(SearchCriteria searchCriteria) {
        try {
            log.debug("getCategoriesGridData: {}", searchCriteria);
            long totalRecords = criteriaRepository.getCountBySearchCriteria(UserCategory.class, searchCriteria);
            List<UserCategoryDto> recordDetails = userCategoryMapper.toDtoList(criteriaRepository.getDataBySearchCriteria(UserCategory.class, searchCriteria));
            log.debug("getCategoriesGridData: totalRecords: {}", totalRecords);
            return GridResult.builder().totalRecords(totalRecords).recordDetails(recordDetails).build();
        } catch (Exception e) {
            log.error("Error occurred while fetching grid data for criteria: {}", searchCriteria, e);
            return GridResult.builder().totalRecords(0).recordDetails(Collections.emptyList()).build();
        }
    }
}
