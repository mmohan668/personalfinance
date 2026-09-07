package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridColumnDto;
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
    private List<GridColumnDto> fetchGridColumns(@RequestParam String gridName) {
        return gridService.fetchGridColumns(gridName);
    }

}
