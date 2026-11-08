package com.pf.warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://192.168.0.101:1995",
                        "http://localhost:4200")
                .allowedHeaders("*")
                .exposedHeaders("X-New-Access-Token")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
