package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.dto.gp.GridPersonalizationDto;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.service.gp.GridService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grid")
public class GridController {
    private final GridService gridService;

    @GetMapping("/fetchGridColumns")
    public List<GridColumnDto> fetchGridColumns(@RequestParam String gridName, @RequestParam Long userId) {
        return gridService.fetchGridColumns(gridName, userId);
    }

    @PostMapping("/saveGridSettings")
    public ApiResponse saveGridSettings(@RequestBody GridPersonalizationDto gridPersonalizationDto) {
        return gridService.saveGridSettings(gridPersonalizationDto);
    }

    @PostMapping("/resetGridSettings")
    public ApiResponse resetGridSettings(@RequestBody GridPersonalizationDto gridPersonalizationDto) {
        return gridService.resetGridPersonalization(gridPersonalizationDto);
    }
}
