package com.pf.common.service.userManagement;

import com.pf.common.constants.CommonConstants;
import com.pf.common.dto.generic.ForgotPasswordEmailRequest;
import com.pf.common.dto.userManagement.ForgotPasswordRequest;
import com.pf.common.entity.userManagement.User;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import com.pf.common.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "warehouse"
)
public class ForgotPasswordService {

    private final CaptchaService captchaService;
    private final UserRepository userRepository;
    private final ActiveMqProducer activeMqProducer;

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

    private static final int TEMP_PASSWORD_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public void processRequest(ForgotPasswordRequest request) {

        boolean captchaValid = captchaService.validateCaptcha(
                request.getCaptchaId(),
                request.getCaptchaAnswer()
        );

        if (!captchaValid) {
            throw new IllegalArgumentException(
                    "Invalid or expired CAPTCHA"
            );
        }

        User user = userRepository
                .findByUsernameAndDisplayNameAndEmail(
                        request.getUsername(),
                        request.getDisplayName(),
                        request.getEmail()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid account details"
                ));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("Invalid account details");
        }

        String temporaryPassword = generateTemporaryPassword();

        user.setPasswordHash(PasswordUtils.encode(temporaryPassword));
        user.setMustChangePassword(true);

        userRepository.save(user);

        activeMqProducer.sendMessage(
                CommonConstants.FORGOT_PASSWORD_MAIL,
                new ForgotPasswordEmailRequest(
                        user.getEmail(),
                        temporaryPassword
                )
        );

    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = secureRandom.nextInt(PASSWORD_CHARACTERS.length());
            password.append(PASSWORD_CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}
