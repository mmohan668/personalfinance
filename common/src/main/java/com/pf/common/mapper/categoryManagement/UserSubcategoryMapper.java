package com.pf.common.mapper.categoryManagement;

import com.pf.common.dto.categoryManagement.UserSubcategoryDto;

import com.pf.common.entity.categoryManagement.UserSubcategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserSubcategoryMapper {

    @Mapping(source = "userCategory.id", target = "userCategoryId")
    UserSubcategoryDto toDto(UserSubcategory entity);

    @Mapping(source = "userCategoryId", target = "userCategory.id")
    UserSubcategory toEntity(UserSubcategoryDto dto);

    List<UserSubcategoryDto> toDtoList(List<UserSubcategory> entities);
}
