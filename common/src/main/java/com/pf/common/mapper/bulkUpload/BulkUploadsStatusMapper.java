package com.pf.common.mapper.bulkUpload;

import com.pf.common.dto.bulkUpload.BulkUploadsStatusDto;
import com.pf.common.entity.bulkUpload.BulkUploadsStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BulkUploadsStatusMapper {

    @Mapping(source = "createdBy.username", target = "createdBy")
    BulkUploadsStatusDto toDto(BulkUploadsStatus entity);

    List<BulkUploadsStatusDto> toDtoList(List<BulkUploadsStatus> entityList);

    @Mapping(target = "createdBy", ignore = true)
    BulkUploadsStatus toEntity(BulkUploadsStatusDto dto);
}
