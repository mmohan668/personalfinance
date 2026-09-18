package com.pf.warehouse.controller;

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

    @GetMapping("/fetchCategoryTypes")
    private List<SelectItem> fetchCategoryTypes() {
        return categoryManagementService.fetchCategoryTypes();
    }

    @PostMapping("/saveCategory")
    private ApiResponse saveCategory(@RequestBody UserCategoryDto userCategoryDto) {
        return categoryManagementService.saveCategory(userCategoryDto);
    }

    @PostMapping("/deleteCategories")
    private ApiResponse deleteCategories(@RequestBody List<Long> ids) {
        return categoryManagementService.deleteCategories(ids);
    }
}
