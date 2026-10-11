package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.service.export.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportController {
    private final ExportService exportService;

    @PostMapping("/exportServer")
    public ApiResponse exportServer(@RequestBody SearchCriteria searchCriteria) {
        return exportService.exportServer(searchCriteria);
    }
}
