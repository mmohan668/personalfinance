package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.ReferenceObjectDto;
import com.pf.common.entity.settings.ReferenceObject;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceObjectMapper {
    ReferenceObjectDto toDto(ReferenceObject referenceObject);

    ReferenceObject toEntity(ReferenceObjectDto referenceObjectDto);

    List<ReferenceObjectDto> toDtoList(List<ReferenceObject> referenceObjects);
}
