package com.pf.common.dto.settings;

import com.pf.common.dto.BaseDto;
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
public class SystemConfigDto extends BaseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            CONFIG_VALUE, REFERENCE_VALUE_REFERENCE_CODE,
            CREATED_BY, CREATED_BY_USERNAME,
            UPDATED_BY, UPDATED_BY_USERNAME
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
