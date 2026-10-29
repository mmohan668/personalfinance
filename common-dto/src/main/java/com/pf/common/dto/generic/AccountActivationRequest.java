package com.pf.common.dto.generic;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountActivationRequest {
    String emailId;
    String token;
}
