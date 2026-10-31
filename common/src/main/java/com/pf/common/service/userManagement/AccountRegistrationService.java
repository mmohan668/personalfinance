package com.pf.common.service.userManagement;

import com.pf.common.constants.CommonConstants;
import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import com.pf.common.service.generic.BaseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static com.pf.common.constants.CommonConstants.SYSTEM;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "warehouse"
)
@RequiredArgsConstructor
public class AccountRegistrationService extends BaseService {
    private final ActivationTokenService activationTokenService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityManager entityManager;
    private final ActiveMqProducer activeMqProducer;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse signUp(UserDto dto) {
        String username = dto.getUsername().trim();

        log.debug("Signup request received for username: {}", username);

        if (userRepository.existsByUsername(username.toLowerCase(Locale.ROOT).trim())) {
            log.warn("Signup failed: username '{}' is already in use", username);
            return failure("Username is already taken. Please choose another username.");
        }

        if (userRepository.existsByEmail(dto.getEmail().toLowerCase(Locale.ROOT).trim())) {
            log.warn("Signup failed: email '{}' is already in use", dto.getEmail());
            return failure("Email is already taken. Please choose another email.");
        }

        User user = userMapper.toEntity(dto);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setIsAdminUser(Boolean.TRUE);
        user.setCreatedBy(SYSTEM);

        User savedUser = userRepository.save(user);

        generateActivationTokenAndSend(savedUser);

        log.info("User account created successfully. Awaiting email activation for username: {}", username);

        return success(
                "Account created successfully! Please check your inbox to activate your account. " +
                        "If you don't see the email, please check your Spam or Junk folder.",
                savedUser.getId()
        );
    }

    private void generateActivationTokenAndSend(User user) {
        String token = activationTokenService.generateToken(user.getId());
        activeMqProducer.sendMessage(
                CommonConstants.ACCOUNT_ACTIVATION_MAIL,
                AccountActivationRequest.builder()
                        .emailId(user.getEmail())
                        .token(token)
                        .build()
        );

    }

    @Transactional
    public String activateAccount(String token) {
        log.debug("Activate account request received for token: {}", token);
        Long userId = activationTokenService.validateAndGetUserId(token);
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "This account does not exist. Please sign up again."
            );
        }
        if (userRepository.alreadyActive(userId)) {
            throw new IllegalArgumentException(
                    "This account is already active. Please log in."
            );
        }
        log.info("Starting account activation and initialization for user ID: {}", userId);
        try {
            entityManager
                    .createStoredProcedureQuery("initialize_new_user")
                    .registerStoredProcedureParameter(
                            1, Long.class, ParameterMode.IN)
                    .setParameter(1, userId)
                    .execute();

            log.info(
                    "Account activated and initialized successfully for user ID: {}",
                    userId
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to activate and initialize account for user ID: {}",
                    userId,
                    ex
            );

            throw ex;
        }
        return "Account activated successfully. Please log in.";
    }
}
