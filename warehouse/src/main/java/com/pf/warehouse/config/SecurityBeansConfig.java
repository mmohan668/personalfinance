package com.pf.warehouse.config;

import com.pf.warehouse.filter.JwtSessionValidationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public FilterRegistrationBean<JwtSessionValidationFilter> jwtSessionValidationFilterRegistration(
            JwtSessionValidationFilter jwtSessionValidationFilter) {
        FilterRegistrationBean<JwtSessionValidationFilter> registration =
                new FilterRegistrationBean<>(jwtSessionValidationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtSessionValidationFilter jwtSessionValidationFilter
    ) {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/signUp",
                                "/api/auth/activateAccount",
                                "/api/auth/login",
                                "/api/auth/google",
                                "/api/auth/google/client-id",
                                "/api/auth/captcha",
                                "/api/auth/forgot-password",
                                "/api/recurringTransactions/scheduler/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(
                        jwtSessionValidationFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }
}
