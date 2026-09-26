package com.pf.common.service.categoryManagement;

import com.google.common.collect.Lists;
import com.pf.common.dto.categoryManagement.SubcategoryViewDto;
import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.dto.generic.SelectItem;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.categoryManagement.SubcategoryView;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.categoryManagement.SubcategoryViewMapper;
import com.pf.common.mapper.categoryManagement.UserCategoryMapper;
import com.pf.common.repository.categoryManagement.UserCategoryRepository;
import com.pf.common.repository.categoryManagement.UserSubcategoryRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.pf.common.constants.CommonConstants.*;
import static com.pf.common.constants.EntityConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryManagementService extends BaseService {
    private final UserCategoryMapper userCategoryMapper;
    private final SubcategoryViewMapper subcategoryViewMapper;
    private final ReferenceValueRepository referenceValueRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final UserSubcategoryRepository userSubcategoryRepository;

    public GridResult fetchCategoriesGridData(SearchCriteria searchCriteria) {
        log.debug("fetchCategoriesGridData: {}", searchCriteria);
        searchCriteria.setFetchPaths(List.of(REFERENCE_VALUE, CREATED_BY_USER, UPDATED_BY_USER));
        searchCriteria.setFIELD_MAPPINGS(UserCategoryDto.FIELD_MAPPINGS);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(UserCategory.class, searchCriteria);
        }
        List<UserCategoryDto> recordDetails = userCategoryMapper.toDtoList(getDataBySearchCriteria(UserCategory.class, searchCriteria));
        log.debug("fetchCategoriesGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    public GridResult fetchSubcategoriesGridData(SearchCriteria searchCriteria) {
        log.debug("fetchSubcategoriesGridData: {}", searchCriteria);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(SubcategoryView.class, searchCriteria);
        }
        List<SubcategoryViewDto> recordDetails = subcategoryViewMapper.toDtoList(getDataBySearchCriteria(SubcategoryView.class, searchCriteria));
        log.debug("fetchSubcategoriesGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveCategory(UserCategoryDto userCategoryDto) {
        log.debug("saveCategory: {}", userCategoryDto);
        ReferenceValue referenceValue = referenceValueRepository.findById(userCategoryDto.getTransactionTypeId()).orElse(null);
        if (referenceValue == null) {
            log.warn("saveCategory: referenceValue is null");
            return failure("Selected Transaction Type not found");
        }
        User user = fetchLoginUser();
        if (userCategoryDto.getId() == null) {
            log.debug("addCategory: userCategoryDto = {}", userCategoryDto);
            UserCategory userCategory = userCategoryMapper.toEntity(userCategoryDto);
            userCategory.setCreatedBy(user);
            if (
                    userCategoryRepository.countByTransactionTypeAndNameAndUserId(
                            userCategoryDto.getTransactionTypeId(),
                            userCategoryDto.getCategoryName(),
                            user.getAdminUser().getId()
                    ) > 0) {
                log.warn(
                        "Category already exists for username = {} for Transaction Type = {}",
                        user.getUsername(),
                        userCategory.getCategoryName()
                );
                return failure("Category already exists");
            }
            userCategory.setUser(user.getAdminUser());
            userCategory.setReferenceValue(referenceValue);
            userCategoryRepository.save(userCategory);
        } else {
            log.warn("updateCategory: userCategoryDto = {}", userCategoryDto);
            UserCategory userCategory = userCategoryRepository.findById(userCategoryDto.getId()).orElse(null);
            if (userCategory == null) {
                log.warn("updateCategory: userCategory not found for category name = {}", userCategoryDto.getCategoryName());
                return failure("Category not found to update");
            }
            if (userCategoryRepository.countByTransactionTypeAndNameAndUserIdAndIdNot(
                    userCategoryDto.getTransactionTypeId(),
                    userCategoryDto.getCategoryName(),
                    user.getAdminUser().getId(),
                    userCategory.getId()
            ) > 0) {
                log.warn(
                        "Category already exists for username = {} for category name = {}",
                        user.getUsername(),
                        userCategoryDto.getCategoryName()
                );
                return failure("Category already exists");
            }
            userCategory.setReferenceValue(referenceValue);
            userCategory.setUser(user);
            userCategory.setCategoryName(userCategoryDto.getCategoryName());
            userCategory.setCategoryDescription(userCategoryDto.getCategoryDescription());
            userCategory.setUpdatedBy(user);
            userCategoryRepository.save(userCategory);
        }
        return success("Category saved successfully");
    }

    @Transactional
    public ApiResponse deleteCategories(List<Long> ids) {
        log.debug("deleteCategories: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("deleteCategories: ids is empty");
            return failure("Ids is empty");
        }
        if (userSubcategoryRepository.countByCategoryId(ids) > 0) {
            log.warn("Delete category failed, Subcategories associated with selected categories");
            return failure("Delete category failed. Subcategories associated with selected categories");
        }
        userCategoryRepository.deleteAllByIdInBatch(ids);
        return success("Categories deleted successfully");
    }

    @Transactional
    public ApiResponse activateCategories(List<Long> ids, boolean activateSubcategories) {
        if (ids == null || ids.isEmpty()) {
            log.warn("activateCategories: ids are empty");
            return failure("IDs are empty");
        }
        log.info("Activating {} categories", ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        long categoryCount = 0;
        long subcategoryCount = 0;
        Long userId = fetchLoginUser().getId();
        for (List<Long> chunk : chunks) {
            categoryCount += userCategoryRepository.activateUserCategory(chunk, userId, updatedTime);
            if (activateSubcategories) {
                subcategoryCount += userSubcategoryRepository.activateSubcategory(chunk, userId, updatedTime);
            }
        }
        log.info("Activated categories count: {}", categoryCount);
        log.info("Activated subcategories count: {}", subcategoryCount);
        return success("Categories have been activated successfully.");
    }

    @Transactional
    public ApiResponse inactivateCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("inactivateCategories: ids is empty");
            return failure("IDs are empty");
        }
        log.info("Inactivating {} categories", ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        long categoryCount = 0;
        long subcategoryCount = 0;
        Long userId = fetchLoginUser().getId();
        for (List<Long> chunk : chunks) {
            categoryCount += userCategoryRepository.inactivateUserCategory(chunk, userId, updatedTime);
            subcategoryCount += userSubcategoryRepository.inactivateSubcategory(chunk, userId, updatedTime);
        }
        log.info("Inactivated categories count: {}", categoryCount);
        log.info("Inactivated subcategories count: {}", subcategoryCount);
        return success("Categories and their associated subcategories have been inactivated successfully.");
    }

    public List<SelectItem> fetchCategories(Long referenceValueId) {
        Long adminUserId = fetchLoginUser().getAdminUser().getId();
        return userCategoryRepository.fetchCategoriesByUserId(adminUserId, referenceValueId);
    }

    public List<SelectItem> fetchSubcategoriesByCategory(Long categoryId) {
        return userSubcategoryRepository.fetchSubcategoriesByCategory(categoryId);
    }

    public List<SelectItem> fetchCategoriesByReferenceCode(String referenceCode) {
        Long adminUserId = fetchLoginUser().getAdminUser().getId();
        return userCategoryRepository.fetchCategoriesByReferenceCode(adminUserId, referenceCode);
    }

    @Transactional
    public ApiResponse saveSubcategory(SubcategoryViewDto subcategoryViewDto) {
        log.debug("saveSubcategory: {}", subcategoryViewDto);
        User user = fetchLoginUser();
        UserCategory userCategory = userCategoryRepository.findById(subcategoryViewDto.getUserCategoryId()).orElse(null);
        if (userCategory == null) {
            log.warn("saveSubcategory: userCategory is null");
            return failure("Selected category not found");
        }
        if (subcategoryViewDto.getId() == null) {
            if (userSubcategoryRepository.countByCategoryIdAndSubcategoryName(
                    subcategoryViewDto.getUserCategoryId(),
                    subcategoryViewDto.getSubcategoryName()
            ) > 0) {
                return failure("Subcategory already exists in the selected category");
            }
            UserSubcategory userSubcategory = new UserSubcategory();
            userSubcategory.setUserCategory(userCategory);
            userSubcategory.setSubcategoryName(subcategoryViewDto.getSubcategoryName());
            userSubcategory.setSubcategoryDescription(subcategoryViewDto.getSubcategoryDescription());
            userSubcategory.setCreatedBy(user);
            userSubcategoryRepository.save(userSubcategory);
        } else {
            UserSubcategory userSubcategory = userSubcategoryRepository.findById(subcategoryViewDto.getId()).orElse(null);
            if (userSubcategory == null) {
                log.warn("saveSubcategory: userSubcategory is null");
                return failure("Subcategory not found");
            }
            if (userSubcategoryRepository.countByCategoryIdAndSubcategoryNameAndIdNot(
                    subcategoryViewDto.getUserCategoryId(),
                    subcategoryViewDto.getSubcategoryName(),
                    subcategoryViewDto.getId()
            ) > 0) {
                return failure("Subcategory already exists in the selected category");
            }
            userSubcategory.setUserCategory(userCategory);
            userSubcategory.setSubcategoryName(subcategoryViewDto.getSubcategoryName());
            userSubcategory.setSubcategoryDescription(subcategoryViewDto.getSubcategoryDescription());
            userSubcategory.setUpdatedBy(user);
        }
        return success("Subcategory saved successfully");
    }

    @Transactional
    public ApiResponse deleteSubcategories(List<Long> ids) {
        log.debug("deleteSubcategories: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("deleteSubcategories: ids is empty");
            return failure("Ids is empty");
        }
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            userSubcategoryRepository.deleteAllByIdInBatch(chunk);
        }
        return success("Categories deleted successfully");
    }

    @Transactional
    public ApiResponse activateSubcategories(List<Long> ids) {
        log.debug("activateSubcategories: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("activateSubcategories: ids is empty");
            return failure("Ids are empty");
        }
        Long userId = fetchLoginUser().getId();
        long count = 0;
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            count += userSubcategoryRepository.activateSubcategories(chunk, userId, updatedTime);
        }
        log.info("Activated {} subcategories", count);
        return success("Subcategories activated successfully");
    }

    @Transactional
    public ApiResponse inactivateSubcategories(List<Long> ids) {
        log.debug("deactivateSubcategories: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("deactivateSubcategories: ids is empty");
            return failure("Ids are empty");
        }
        Long userId = fetchLoginUser().getId();
        long count = 0;
        LocalDateTime updatedTime = LocalDateTime.now();
        List<List<Long>> chunks = Lists.partition(ids, CHUNK_SIZE);
        for (List<Long> chunk : chunks) {
            count += userSubcategoryRepository.inactivateSubcategories(chunk, userId, updatedTime);
        }
        log.info("Inactivated {} subcategories", count);
        return success("Subcategories inactivated successfully");
    }
}
