package com.pf.common.mapper.categoryManagement;

import com.pf.common.dto.categoryManagement.UserCategoryDto;
import com.pf.common.entity.categoryManagement.UserCategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = UserSubcategoryMapper.class
)
public interface UserCategoryMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "referenceObject.refObjName", target = "categoryType")
        UserCategoryDto toDto(UserCategory entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "subcategories", ignore = true)
    UserCategory toEntity(UserCategoryDto dto);

    List<UserCategoryDto> toDtoList(List<UserCategory> entities);
}