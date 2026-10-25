package com.pf.common.dto.export;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExportFileDto {
    private String groupName;
    private String fileName;
    private String filePath;
    private Instant dateCreated;
    private long size;
}
