package com.pf.common.dto.userManagement;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassword {
    Long userId;
    String currentPassword;
    String newPassword;
}
