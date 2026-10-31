package com.pf.common.mapper.userManagement;

import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.dto.userManagement.UserProfile;
import com.pf.common.entity.userManagement.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "adminUser", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "displayName", source = "username")
    @Mapping(target = "adminUserId", source = "adminUser.id")
    @Mapping(target = "imageUrl", ignore = true)
    UserProfile toUserProfile(User user);

    List<UserDto> toDtoList(List<User> entities);

}
