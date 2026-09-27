package com.pf.common.dto.settings;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "configValue", "referenceValue.referenceCode",
            "createdBy", "createdBy.username",
            "updatedBy", "updatedBy.username"
    );

    private Long id;
    private String configName;
    private String configValue;
    private Long referenceId;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
