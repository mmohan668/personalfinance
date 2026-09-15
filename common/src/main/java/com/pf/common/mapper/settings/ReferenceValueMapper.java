package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.entity.settings.ReferenceValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceValueMapper {
    @Mapping(target = "refObjName", source = "referenceObject.refObjName")
    ReferenceValueDto toDto(ReferenceValue entity);

    ReferenceValue toEntity(ReferenceValueDto dto);

    List<ReferenceValueDto> toDtoList(List<ReferenceValue> entityList);

    void updateEntity(
            ReferenceValueDto dto,
            @MappingTarget ReferenceValue entity
    );
}
