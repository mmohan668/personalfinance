package com.pf.common.mapper.userManagement;

import com.pf.common.dto.userManagement.UserPermissionViewDto;
import com.pf.common.dto.userManagement.AppPermission;
import com.pf.common.entity.userManagement.UserPermissionView;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPermissionViewMapper {

    UserPermissionViewDto toDto(UserPermissionView userPermissionView);

    List<UserPermissionViewDto> toDtoList(List<UserPermissionView> userPermissionViews);

    AppPermission toAppPermission(UserPermissionViewDto userPermissionViewDto);

    List<AppPermission> toAppPermissionList(List<UserPermissionViewDto> userPermissionViewDtos);
}
