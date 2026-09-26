package com.pf.warehouse.controller;

import com.pf.common.dto.categoryManagement.SubcategoryViewDto;
import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.dto.generic.SelectItem;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.service.categoryManagement.CategoryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/categoryManagement")
@RestController
@RequiredArgsConstructor
public class CategoryManagementController {
    private final CategoryManagementService categoryManagementService;

    @PostMapping("/fetchCategoriesGridData")
    public GridResult fetchCategoriesGridData(@RequestBody SearchCriteria searchCriteria) {
        return categoryManagementService.fetchCategoriesGridData(searchCriteria);
    }

    @PostMapping("/fetchSubcategoriesGridData")
    public GridResult fetchSubcategoriesGridData(@RequestBody SearchCriteria searchCriteria) {
        return categoryManagementService.fetchSubcategoriesGridData(searchCriteria);
    }

    @PostMapping("/saveCategory")
    public ApiResponse saveCategory(@RequestBody UserCategoryDto userCategoryDto) {
        return categoryManagementService.saveCategory(userCategoryDto);
    }

    @PostMapping("/saveSubcategory")
    public ApiResponse saveSubcategory(@RequestBody SubcategoryViewDto subcategoryViewDto) {
        return categoryManagementService.saveSubcategory(subcategoryViewDto);
    }

    @PostMapping("/deleteCategories")
    public ApiResponse deleteCategories(@RequestBody List<Long> ids) {
        return categoryManagementService.deleteCategories(ids);
    }

    @PostMapping("/deleteSubcategories")
    public ApiResponse deleteSubcategories(@RequestBody List<Long> ids) {
        return categoryManagementService.deleteSubcategories(ids);
    }

    @PostMapping("/activateCategories")
    public ApiResponse activateCategories(@RequestBody List<Long> ids, @RequestParam boolean activateSubcategories) {
        return categoryManagementService.activateCategories(ids, activateSubcategories);
    }

    @PostMapping("/inactivateCategories")
    public ApiResponse inactivateCategories(@RequestBody List<Long> ids) {
        return categoryManagementService.inactivateCategories(ids);
    }

    @GetMapping("/fetchCategories")
    public List<SelectItem> fetchCategories(@RequestParam Long referenceValueId) {
        return categoryManagementService.fetchCategories(referenceValueId);
    }

    @GetMapping("/fetchSubcategoriesByCategory")
    public List<SelectItem> fetchSubcategoriesByCategory(@RequestParam Long categoryId) {
        return categoryManagementService.fetchSubcategoriesByCategory(categoryId);
    }

    @GetMapping("/fetchCategoriesByReferenceCode")
    public List<SelectItem> fetchCategoriesByReferenceCode(@RequestParam String referenceCode) {
        return categoryManagementService.fetchCategoriesByReferenceCode(referenceCode);
    }

    @PostMapping("/activateSubcategories")
    public ApiResponse activateSubcategories(@RequestBody List<Long> ids) {
        return categoryManagementService.activateSubcategories(ids);
    }

    @PostMapping("/inactivateSubcategories")
    public ApiResponse inactivateSubcategories(@RequestBody List<Long> ids) {
        return categoryManagementService.inactivateSubcategories(ids);
    }
}
