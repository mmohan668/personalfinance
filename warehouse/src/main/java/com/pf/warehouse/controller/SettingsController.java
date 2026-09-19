package com.pf.warehouse.controller;

import com.pf.common.dto.generic.SelectItem;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.service.settings.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @PostMapping("/fetchReferenceObjectGridData")
    public GridResult fetchReferenceObjectGridData(@RequestBody SearchCriteria searchCriteria) {
        return settingsService.fetchReferenceObjectGridData(searchCriteria);
    }

    @PostMapping("/fetchReferenceValueGridData")
    public GridResult fetchReferenceValueGridData(@RequestBody SearchCriteria searchCriteria) {
        return settingsService.fetchReferenceValueGridData(searchCriteria);
    }

    @PostMapping("/saveReferenceValue")
    public ApiResponse saveReferenceValue(
            @Valid @RequestBody ReferenceValueDto referenceValueDto) {
        return settingsService.saveReferenceValue(referenceValueDto);
    }

    @PostMapping("/deleteReferenceValue")
    public ApiResponse deleteReferenceValue(@RequestBody List<Long> ids) {
        return settingsService.deleteReferenceValue(ids);
    }

    @GetMapping("/fetchCategoryTypes")
    public List<SelectItem> fetchCategoryTypes() {
        return settingsService.fetchCategoryTypes();
    }
}
