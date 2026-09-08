package com.pf.common.mapper.categoryManagement;

import com.pf.common.dto.categoryManagement.SubcategoryViewDto;
import com.pf.common.entity.categoryManagement.SubcategoryView;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubcategoryViewMapper {
    SubcategoryViewDto toDto(SubcategoryView subcategoryView);

    List<SubcategoryViewDto> toDto(List<SubcategoryView> subcategoryViews);
}
