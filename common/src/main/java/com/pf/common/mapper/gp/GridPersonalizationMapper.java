package com.pf.common.mapper.gp;

import com.pf.common.dto.gp.GridPersonalizationDto;
import com.pf.common.entity.gp.GridPersonalization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GridPersonalizationMapper {
    @Mapping(source = "gridName.id", target = "gridNameId")
    GridPersonalizationDto toDto(GridPersonalization entity);

    @Mapping(source = "gridNameId", target = "gridName.id")
    GridPersonalization toEntity(GridPersonalizationDto dto);
}
