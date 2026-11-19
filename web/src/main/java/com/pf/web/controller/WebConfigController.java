package com.pf.web.controller;

import com.pf.web.config.WebAppConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebConfigController {
    private final WebAppConfigProperties webAppConfigProperties;

    @GetMapping("/api/config")
    public WebAppConfigProperties fetchWebConfig() {
        return webAppConfigProperties;
    }
}
