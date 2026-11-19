package com.pf.common.mapper.bulkUpload;

import com.pf.common.dto.bulkUpload.InvoiceUploadStatusDto;
import com.pf.common.entity.bulkUpload.InvoiceUploadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceUploadStatusMapper {
    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    InvoiceUploadStatusDto toDto(InvoiceUploadStatus entity);

    List<InvoiceUploadStatusDto> toDtoList(List<InvoiceUploadStatus> entities);
}
