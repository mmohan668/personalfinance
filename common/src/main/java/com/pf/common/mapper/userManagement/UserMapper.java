package com.pf.common.mapper.userManagement;

import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "adminUser.id", target = "adminUserId")
    UserDto toDto(User entity);

    @Mapping(source = "adminUserId", target = "adminUser.id")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "managedUsers", ignore = true)
    User toEntity(UserDto dto);

    List<UserDto> toDtoList(List<User> entities);

}
