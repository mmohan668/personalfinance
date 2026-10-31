package com.pf.common.dto.userManagement;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile {
    private String username;
    private String email;
    private String displayName;
    private Long id;
    private Long adminUserId;
    private String profilePictureUrl;
}
