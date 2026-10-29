package com.pf.common.dto.userManagement;

import com.pf.common.dto.BaseDto;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto extends BaseDto implements Serializable {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            ADMIN_USER_ID, ADMIN_USER__ID
    );

    private Long id;
    private String username;
    private String displayName;
    private String password;
    private String email;
    private Boolean active;
    private Boolean isAdminUser;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
