package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.userManagement.LoginRequest;
import com.pf.common.dto.userManagement.LoginResponse;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.record.jwt.GeneratedJwt;
import com.pf.common.service.redis.RedisSessionService;
import com.pf.common.service.userManagement.AccountRegistrationService;
import com.pf.common.service.userManagement.AuthenticationService;
import com.pf.warehouse.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountRegistrationService accountRegistrationService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RedisSessionService redisSessionService;

    @PostMapping("/signUp")
    public ApiResponse signUp(@RequestBody UserDto userDto) {
        return accountRegistrationService.signUp(userDto);
    }

    @GetMapping("/activateAccount")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        return ResponseEntity.ok(accountRegistrationService.activateAccount(token));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authenticationService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        String sessionId = UUID.randomUUID().toString();
        GeneratedJwt generatedJwt = jwtService.generateToken(user, sessionId);
        redisSessionService.setCurrentTokenId(sessionId, generatedJwt.tokenId());

        return ResponseEntity.ok(
                new LoginResponse(
                        generatedJwt.token(),
                        "Bearer",
                        900,
                        userMapper.toUserProfile(user)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt) {

        String sessionId = jwt.getClaimAsString("sid");

        if (sessionId != null) {
            redisSessionService.deleteSession(sessionId);
        }

        return ResponseEntity.noContent().build();
    }
}

