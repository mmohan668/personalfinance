package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.service.gp.GridService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pf-warehouse/grid")
public class GridController {
    private final GridService gridService;

    @GetMapping("/getColumns")
    private List<GridColumnDto> getColumns(@RequestParam String gridName) {
        return gridService.getGridColumns(gridName);
    }

    @PostMapping("/getData")
    private GridResult getData(@RequestBody SearchCriteria searchCriteria) {
        return gridService.getProducts(searchCriteria);
    }
}
