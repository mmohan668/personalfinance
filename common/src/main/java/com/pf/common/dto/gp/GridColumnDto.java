package com.pf.common.dto.gp;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridColumnDto {
    private Long id;
    private Long gridNameId;
    private String field;
    private String header;
    private String dataType;
    private Boolean sortable;
    private String defaultSortOrder;
    private Boolean filterable;
    private String defaultFilterOperator;
    private Integer width;
    private String align;
    private Boolean visible;
    private Integer visibleIndex;
    private String cellTemplate;
    private Integer sortIndex;
}
