package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.entity.settings.ReferenceValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceValueMapper {
    @Mapping(target = "refObjName", source = "referenceObject.refObjName")
    @Mapping(target = "refObjNameId", source = "referenceObject.id")
    @Mapping(target = "createdBy", source = "createdBy.username")
    @Mapping(target = "updatedBy", source = "updatedBy.username")
    ReferenceValueDto toDto(ReferenceValue entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "user", ignore = true)
    ReferenceValue toEntity(ReferenceValueDto dto);

    List<ReferenceValueDto> toDtoList(List<ReferenceValue> entityList);

}
