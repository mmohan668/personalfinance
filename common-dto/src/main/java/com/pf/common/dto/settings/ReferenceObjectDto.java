package com.pf.common.dto.settings;

import com.pf.common.dto.BaseDto;
import jakarta.validation.constraints.NotBlank;
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
public class ReferenceObjectDto extends BaseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            CREATED_BY, CREATED_BY_USERNAME,
            UPDATED_BY, UPDATED_BY_USERNAME
    );

    private Long id;
    @NotBlank(message = "Reference object name is required")
    private String refObjName;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
