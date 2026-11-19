package com.pf.warehouse.service;

import com.pf.common.entity.userManagement.User;
import com.pf.common.record.jwt.GeneratedJwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

@Service
public class JwtService {
    public static final long ACCESS_TOKEN_LIFETIME_SECONDS = 30 * 60L;
    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofSeconds(ACCESS_TOKEN_LIFETIME_SECONDS);

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public GeneratedJwt generateToken(User user, String sessionId) {
        Instant now = Instant.now();

        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("warehouse")
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_LIFETIME))
                .subject(user.getId().toString())
                .id(tokenId)
                .claim("sid", sessionId)
                .claim("username", user.getUsername())
                .claim("admin", Boolean.TRUE.equals(user.getIsAdminUser()))
                .claim("userId", user.getId())
                .claim("adminUserId", user.getAdminUser().getId())
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new GeneratedJwt(token, tokenId);
    }

}
