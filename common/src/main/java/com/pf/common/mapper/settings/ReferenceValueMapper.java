package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.ReferenceValueDto;
import com.pf.common.entity.settings.UserReferenceValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceValueMapper {
    @Mapping(target = "refObjName", source = "referenceObject.refObjName")
    @Mapping(target = "refObjNameId", source = "referenceObject.id")
    @Mapping(target = "createdBy", source = "createdBy.username")
    @Mapping(target = "updatedBy", source = "updatedBy.username")
    ReferenceValueDto toDto(UserReferenceValue entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserReferenceValue toEntity(ReferenceValueDto dto);

    List<ReferenceValueDto> toDtoList(List<UserReferenceValue> entityList);

}
