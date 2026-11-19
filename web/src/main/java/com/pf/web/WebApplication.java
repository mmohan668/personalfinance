package com.pf.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.pf.web.config.WebAppConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(WebAppConfigProperties.class)
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
