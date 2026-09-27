package com.pf.warehouse.controller;

import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.generic.SystemConfigValue;
import com.pf.common.service.generic.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {
    private final CommonService commonService;

    @GetMapping("/fetchSystemConfig")
    public SystemConfigValue fetchSystemConfig() {
        return commonService.fetchSystemConfig();
    }
}
