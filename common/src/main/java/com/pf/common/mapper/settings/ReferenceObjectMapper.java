package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.entity.settings.ReferenceObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceObjectMapper {
    @Mapping(target = "createdBy", source = "createdBy.username")
    @Mapping(target = "updatedBy", source = "updatedBy.username")
    ReferenceObjectDto toDto(ReferenceObject referenceObject);

    List<ReferenceObjectDto> toDtoList(List<ReferenceObject> referenceObjects);
}
