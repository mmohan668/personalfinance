package com.pf.common.mapper.settings;

import com.pf.common.dto.settings.SystemConfigDto;
import com.pf.common.entity.settings.SystemConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SystemConfigMapper {
    @Mapping(source = "configValue.referenceCode", target = "configValue")
    @Mapping(source = "configValue.id", target = "referenceId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    SystemConfigDto toDto(SystemConfig systemConfig);

    List<SystemConfigDto> toDtoList(List<SystemConfig> systemConfigs);
}
