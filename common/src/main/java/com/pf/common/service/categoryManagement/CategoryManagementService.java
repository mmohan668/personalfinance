package com.pf.common.service.categoryManagement;

import com.google.common.collect.Lists;
import com.pf.common.dto.categoryManagement.SubcategoryViewDto;
import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.dto.generic.SelectItem;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.categoryManagement.SubcategoryView;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.categoryManagement.SubcategoryViewMapper;
import com.pf.common.mapper.categoryManagement.UserCategoryMapper;
import com.pf.common.repository.categoryManagement.UserCategoryRepository;
import com.pf.common.repository.categoryManagement.UserSubcategoryRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.criteria.GenericCriteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.pf.common.constants.CommonConstants.TEST_USER;
import static com.pf.common.constants.CommonConstants.CHUNK_SIZE;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryManagementService {
    private final GenericCriteriaService criteriaService;
    private final UserCategoryMapper userCategoryMapper;
    private final SubcategoryViewMapper subcategoryViewMapper;
    private final ReferenceValueRepository referenceValueRepository;
    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final UserSubcategoryRepository userSubcategoryRepository;

    public GridResult fetchCategoriesGridData(SearchCriteria searchCriteria) {
        log.debug("fetchCategoriesGridData: {}", searchCriteria);
        searchCriteria.setFetchPaths(List.of("referenceValue"));
        searchCriteria.setFIELD_MAPPINGS(UserCategoryDto.FIELD_MAPPINGS);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = criteriaService.getCountBySearchCriteria(UserCategory.class, searchCriteria);
        }
        List<UserCategoryDto> recordDetails = userCategoryMapper.toDtoList(criteriaService.getDataBySearchCriteria(UserCategory.class, searchCriteria));
        log.debug("fetchCategoriesGridData: totalRecords: {}", totalRecords);
        return GridResult.builder().totalRecords(totalRecords).recordDetails(recordDetails).build();
    }

    public GridResult fetchSubcategoriesGridData(SearchCriteria searchCriteria) {
        log.debug("fetchSubcategoriesGridData: {}", searchCriteria);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = criteriaService.getCountBySearchCriteria(SubcategoryView.class, searchCriteria);
        }
        List<SubcategoryViewDto> recordDetails = subcategoryViewMapper.toDtoList(criteriaService.getDataBySearchCriteria(SubcategoryView.class, searchCriteria));
        log.debug("fetchSubcategoriesGridData: totalRecords: {}", totalRecords);
        return GridResult.builder().totalRecords(totalRecords).recordDetails(recordDetails).build();
    }

    public List<SelectItem> fetchCategoryTypes() {
        return referenceValueRepository.fetchCategoryTypes();
    }

    @Transactional
    public ApiResponse saveCategory(UserCategoryDto userCategoryDto) {
        log.debug("saveCategory: {}", userCategoryDto);
        ReferenceValue referenceValue = referenceValueRepository.findById(userCategoryDto.getCategoryTypeId()).orElse(null);
        if (referenceValue == null) {
            log.warn("saveCategory: referenceValue is null");
            return ApiResponse.builder()
                    .success(false)
                    .message("Selected category type not found")
                    .build();
        }
        User user = userRepository.findByUsername(TEST_USER);
        if (user == null) {
            log.warn("addCategory: user not found for username = {}", TEST_USER);
            return ApiResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
        }
        if (userCategoryDto.getId() == null) {
            log.debug("addCategory: userCategoryDto = {}", userCategoryDto);
            UserCategory userCategory = userCategoryMapper.toEntity(userCategoryDto);
            userCategory.setCreatedBy(TEST_USER);

            if (
                    userCategoryRepository.countByCategoryTypeAndNameAndUserId(
                            userCategoryDto.getCategoryTypeId(),
                            userCategoryDto.getCategoryName(),
                            user.getAdminUser().getId()
                    ) > 0) {
                log.warn(
                        "Category already exists for username = {} for category type = {}",
                        user.getUsername(),
                        userCategory.getCategoryName()
                );
                return ApiResponse.builder()
                        .success(false)
                        .message("Category already exists")
                        .build();
            }
            userCategory.setUser(user);
            userCategory.setReferenceValue(referenceValue);
            userCategoryRepository.save(userCategory);
        } else {
            log.warn("updateCategory: userCategoryDto = {}", userCategoryDto);
            UserCategory userCategory = userCategoryRepository.findById(userCategoryDto.getId()).orElse(null);
            if (userCategory == null) {
                log.warn("updateCategory: userCategory not found for category name = {}", userCategoryDto.getCategoryName());
                return ApiResponse.builder()
                        .success(false)
                        .message("Category not found to update")
                        .build();
            }
            if (userCategoryRepository.countByCategoryTypeAndNameAndUserIdAndIdNot(
                    userCategoryDto.getCategoryTypeId(),
                    userCategoryDto.getCategoryName(),
                    user.getAdminUser().getId(),
                    userCategory.getId()
            ) > 0) {
                log.warn(
                        "Category already exists for username = {} for category name = {}",
                        user.getUsername(),
                        userCategoryDto.getCategoryName()
                );
                return ApiResponse.builder()
                        .success(false)
                        .message("Category already exists")
                        .build();
            }
            userCategory.setReferenceValue(referenceValue);
            userCategory.setUser(user);
            userCategory.setCategoryName(userCategoryDto.getCategoryName());
            userCategory.setCategoryDescription(userCategoryDto.getCategoryDescription());
            userCategory.setUpdatedBy(TEST_USER);
            userCategoryRepository.save(userCategory);
        }
        return ApiResponse.builder()
                .success(true)
                .message("Category saved successfully")
                .build();
    }

    @Transactional
    public ApiResponse deleteCategories(List<Long> ids) {
        log.debug("deleteCategories: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("deleteCategories: ids is empty");
            return ApiResponse.builder()
                    .success(false)
                    .message("Ids is empty")
                    .build();
        }
        if (userSubcategoryRepository.countByCategoryId(ids) > 0) {
            log.warn("Delete category failed, Subcategories associated with selected categories");
            return ApiResponse.builder()
                    .success(false)
                    .message("Delete category failed. Subcategories associated with selected categories")
                    .build();
        }
        userCategoryRepository.deleteAllByIdInBatch(ids);
        return ApiResponse.builder()
                .success(true)
                .message("Categories deleted successfully")
                .build();
    }

    @Transactional
    public ApiResponse activateCategories(List<Long> ids, boolean activateSubcategories) {
        if (ids == null || ids.isEmpty()) {
            log.warn("activateCategories: ids are empty");
            return ApiResponse.builder()
                    .success(false)
                    .message("IDs are empty")
                    .build();
        }
        log.info("Activating {} categories", ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        long categoryCount = 0;
        long subcategoryCount = 0;
        for (List<Long> chunk : chunks) {
            categoryCount += userCategoryRepository.activateUserCategory(chunk, TEST_USER, updatedTime);
            if (activateSubcategories) {
                subcategoryCount += userSubcategoryRepository.activateSubcategory(chunk, TEST_USER, updatedTime);
            }
        }
        log.info("Activated categories count: {}", categoryCount);
        log.info("Activated subcategories count: {}", subcategoryCount);
        return ApiResponse.builder()
                .success(true)
                .message("Categories have been activated successfully.")
                .build();
    }

    @Transactional
    public ApiResponse inactivateCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("inactivateCategories: ids is empty");
            return ApiResponse.builder()
                    .success(false)
                    .message("IDs are empty")
                    .build();
        }
        log.info("Inactivating {} categories", ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        long categoryCount = 0;
        long subcategoryCount = 0;
        for (List<Long> chunk : chunks) {
            categoryCount += userCategoryRepository.inactivateUserCategory(chunk, TEST_USER, updatedTime);
            subcategoryCount += userSubcategoryRepository.inactivateSubcategory(chunk, TEST_USER, updatedTime);
        }
        log.info("Inactivated categories count: {}", categoryCount);
        log.info("Inactivated subcategories count: {}", subcategoryCount);
        return ApiResponse.builder()
                .success(true)
                .message("Categories and their associated subcategories have been inactivated successfully.")
                .build();
    }
}
