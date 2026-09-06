package com.pf.common.dto.gp;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridPersonalizationDto {
    private Long id;
    private Long gridNameId;
    private Long userId;
    private String gridColumnJson;
}

