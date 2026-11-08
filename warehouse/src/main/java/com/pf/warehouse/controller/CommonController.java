package com.pf.warehouse.controller;

import com.pf.common.dto.generic.SystemConfigValue;
import com.pf.common.service.generic.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {
    private final CommonService commonService;

    @GetMapping("/fetchSystemConfig")
    public SystemConfigValue fetchSystemConfig() {
        return commonService.fetchSystemConfig();
    }

}
