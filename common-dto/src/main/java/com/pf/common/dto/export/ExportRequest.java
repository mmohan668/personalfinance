package com.pf.common.dto.export;

import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.dto.gp.SearchCriteria;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ExportRequest {
    private Long userId;
    private String exportFileName;
    private SearchCriteria searchCriteria;
    List<GridColumnDto> columns;
}
