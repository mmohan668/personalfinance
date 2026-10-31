package com.pf.warehouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtServiceTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void expiredTokenShouldBeRejected() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("warehouse")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .subject("2")
                .claim("username", "MOHAN")
                .claim("admin", true)
                .build();

        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        assertThrows(
                Exception.class,
                () -> jwtDecoder.decode(expiredToken)
        );
    }

    @Test
    void expiredTokenShouldReturn401OverHttp() throws Exception {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("warehouse")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .subject("2")
                .claim("username", "MOHAN")
                .claim("admin", true)
                .build();

        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        mockMvc.perform(get("/common/fetchSystemConfig")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}