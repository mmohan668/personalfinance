package com.pf.common.dto.userManagement;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto implements Serializable {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "adminUserId", "adminUser.id"
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
