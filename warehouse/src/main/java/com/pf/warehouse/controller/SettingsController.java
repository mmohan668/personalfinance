package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.service.settings.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @PostMapping("/fetchReferenceObjectGridData")
    private GridResult fetchReferenceObjectGridData(@RequestBody SearchCriteria searchCriteria) {
        return settingsService.fetchReferenceObjectGridData(searchCriteria);
    }

    @PostMapping("/saveReferenceObject")
    private ApiResponse saveReferenceObject(
            @Valid @RequestBody ReferenceObjectDto referenceObjectDto) {
        return settingsService.saveReferenceObject(referenceObjectDto);
    }
}
