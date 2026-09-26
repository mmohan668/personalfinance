package com.pf.common.mapper.categoryManagement;

import com.pf.common.dto.categoryManagement.UserSubcategoryDto;

import com.pf.common.entity.categoryManagement.UserSubcategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserSubcategoryMapper {

    @Mapping(source = "userCategory.id", target = "userCategoryId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    UserSubcategoryDto toDto(UserSubcategory entity);

    @Mapping(source = "userCategoryId", target = "userCategory.id")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    UserSubcategory toEntity(UserSubcategoryDto dto);

    List<UserSubcategoryDto> toDtoList(List<UserSubcategory> entities);
}
