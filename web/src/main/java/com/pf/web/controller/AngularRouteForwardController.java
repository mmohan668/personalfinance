package com.pf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AngularRouteForwardController {

    @GetMapping("/login")
    public String forwardLogin() {
        return "forward:/index.html";
    }
}
