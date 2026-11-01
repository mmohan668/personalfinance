package com.pf.common.dto.generic;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordEmailRequest {
    private String emailId;
    private String temporaryPassword;
}
