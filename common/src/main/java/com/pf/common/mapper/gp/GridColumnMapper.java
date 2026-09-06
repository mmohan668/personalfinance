package com.pf.common.mapper.gp;

import com.pf.common.dto.gp.GridColumnDto;
import com.pf.common.entity.gp.GridColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GridColumnMapper {
    @Mapping(source = "gridName.id", target = "gridNameId")
    GridColumnDto toDto(GridColumn entity);

    @Mapping(source = "gridNameId", target = "gridName.id")
    GridColumn toEntity(GridColumnDto dto);

    List<GridColumnDto> toDTOList(List<GridColumn> entities);
}
