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
    @Mapping(source = "referenceValue.referenceCode", target = "transactionType")
    @Mapping(source = "referenceValue.id", target = "transactionTypeId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    UserCategoryDto toDto(UserCategory entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    UserCategory toEntity(UserCategoryDto dto);

    List<UserCategoryDto> toDtoList(List<UserCategory> entities);
}