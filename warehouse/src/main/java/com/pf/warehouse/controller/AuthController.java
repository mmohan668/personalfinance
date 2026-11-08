package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.userManagement.*;
import com.pf.common.entity.userManagement.ProfilePicture;
import com.pf.common.entity.userManagement.User;
import com.pf.common.logging.UserLogContext;
import com.pf.common.mapper.userManagement.UserPermissionViewMapper;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.record.jwt.GeneratedJwt;
import com.pf.common.repository.user.ProfilePictureRepository;
import com.pf.common.service.export.ExportService;
import com.pf.common.service.redis.RedisSessionService;
import com.pf.common.service.userManagement.AccountRegistrationService;
import com.pf.common.service.userManagement.AuthenticationService;
import com.pf.common.service.userManagement.CaptchaService;
import com.pf.common.service.userManagement.ForgotPasswordService;
import com.pf.common.service.userManagement.UserService;
import com.pf.warehouse.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountRegistrationService accountRegistrationService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserPermissionViewMapper userPermissionViewMapper;
    private final RedisSessionService redisSessionService;
    private final ProfilePictureRepository profilePictureRepository;
    private final ForgotPasswordService forgotPasswordService;
    private final CaptchaService captchaService;
    private final ExportService exportService;
    private final UserService userService;

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
        try (UserLogContext ignored = UserLogContext.open(user.getId(), user.getUsername())) {
            var permissions = userPermissionViewMapper.toAppPermissionList(
                    userService.fetchPermissionsForUser(user.getId())
            );

            String sessionId = UUID.randomUUID().toString();
            GeneratedJwt generatedJwt = jwtService.generateToken(user, sessionId);
            redisSessionService.setCurrentTokenId(sessionId, generatedJwt.tokenId());
            UserProfile userProfile = userMapper.toUserProfile(user);
            ProfilePicture profilePicture = profilePictureRepository
                    .findByUserId(user.getId())
                    .orElse(null);

            if (profilePicture != null) {
                userProfile.setProfilePictureUrl(
                        "/users/" + user.getUsername() + "/profile-picture"
                );
            } else {
                userProfile.setProfilePictureUrl(null);
            }
            return ResponseEntity.ok(
                    new LoginResponse(
                            generatedJwt.token(),
                            "Bearer",
                            900,
                            userProfile,
                            user.getMustChangePassword(),
                            permissions
                    )
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt) throws IOException {

        String sessionId = jwt.getClaimAsString("sid");
        exportService.deleteUserDirectory();
        if (sessionId != null) {
            redisSessionService.deleteSession(sessionId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        forgotPasswordService.processRequest(request);

        return ResponseEntity.ok(Map.of(
                "message",
                "If the account details are valid, instructions will be sent to your registered email."
        ));
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() throws IOException {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }
}
