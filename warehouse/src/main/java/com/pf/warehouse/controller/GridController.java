package com.pf.warehouse.controller;

import com.pf.common.dto.gp.GridColumnDTO;
import com.pf.common.dto.ProductDTO;
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
    private List<GridColumnDTO> getColumns() {
        return gridService.getGridColumns();
    }

    @PostMapping("/getData")
    private List<ProductDTO> getData(@RequestBody SearchCriteria searchCriteria) {
        return gridService.getProducts(searchCriteria);
    }
}
