package com.pf.warehouse.service;

import com.pf.common.entity.userManagement.User;
import com.pf.common.record.jwt.GeneratedJwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class JwtService {

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
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
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

    public GeneratedJwt generateToken(
            String userId,
            String username,
            boolean admin,
            Long adminUserId,
            String sessionId) {

        Instant now = Instant.now();

        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("warehouse")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(userId)
                .id(tokenId)
                .claim("sid", sessionId)
                .claim("username", username)
                .claim("admin", admin)
                .claim("userId", Long.parseLong(userId))
                .claim("adminUserId", adminUserId)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new GeneratedJwt(token, tokenId);
    }

}
