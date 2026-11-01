package com.pf.common.dto.userManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CaptchaResponse {

    private String captchaId;
    private String captchaImage;
}