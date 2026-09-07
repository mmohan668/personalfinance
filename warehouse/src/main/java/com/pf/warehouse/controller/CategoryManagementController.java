package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.service.categoryManagement.CategoryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/categoryManagement")
@RestController
@RequiredArgsConstructor
public class CategoryManagementController {
    private final CategoryManagementService categoryManagementService;

    @PostMapping("/fetchCategoriesGridData")
    public GridResult fetchCategoriesGridData(@RequestBody SearchCriteria searchCriteria) {
        return categoryManagementService.fetchCategoriesGridData(searchCriteria);
    }
}
