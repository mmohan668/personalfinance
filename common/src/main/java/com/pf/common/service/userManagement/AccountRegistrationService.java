package com.pf.common.service.userManagement;

import com.pf.common.constants.CommonConstants;
import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.repository.user.UserAuthProviderRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.PasswordUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pf.common.entity.userManagement.UserAuthProvider;

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
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Transactional
    public ApiResponse signUp(UserDto dto) {
        String username = dto.getUsername().trim();

        log.info("Signup request received");

        if (userRepository.existsByUsername(username.toLowerCase(Locale.ROOT).trim())) {
            log.warn("Signup failed: username is already in use");
            return failure("Username is already taken. Please choose another username.");
        }

        if (userRepository.existsByEmail(dto.getEmail().toLowerCase(Locale.ROOT).trim())) {
            log.warn("Signup failed: email is already in use");
            return failure("Email is already taken. Please choose another email.");
        }

        User user = userMapper.toEntity(dto);
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.encode(dto.getPassword()));
        user.setIsAdminUser(Boolean.TRUE);
        user.setCreatedBy(SYSTEM);
        user.setMustChangePassword(false);
        User savedUser = userRepository.save(user);

        generateActivationTokenAndSend(savedUser);

        log.info("User account created successfully; awaiting email activation");

        return success(
                "Account created successfully! Please check your inbox to activate your account. " +
                        "If you don't see the email, please check your Spam or Junk folder.",
                savedUser.getId()
        );
    }

    @Transactional
    public User registerOrGetGoogleUser(String email, String googleSubject, String googleDisplayName) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        // Check whether this Google identity is already linked.
        var existingProvider = userAuthProviderRepository
                .findByProviderAndProviderUserId("GOOGLE", googleSubject);

        if (existingProvider.isPresent()) {
            User existingUser = existingProvider.get().getUser();

            if (Boolean.TRUE.equals(existingUser.getActive())
                    && !Boolean.TRUE.equals(existingUser.getIsDeleted())) {
                return existingUser;
            }

            throw new IllegalArgumentException(
                    "This Google account is not linked to an active application account."
            );
        }

        // Do not automatically link Google to an existing email account.
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists. " +
                            "Please sign in using your existing login method."
            );
        }

        String localPart = normalizedEmail.substring(
                0, normalizedEmail.indexOf('@')
        );

        String username = localPart;

        if (userRepository.existsByUsername(username)) {
            username = normalizedEmail;

            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException(
                        "Unable to create an account because the username and email are already in use."
                );
            }
        }

        User user = User.builder()
                .username(username)
                .displayName(googleDisplayName)
                .email(normalizedEmail)
                .passwordHash(PasswordUtils.generateEncodedRandomPassword())
                .mustChangePassword(false)
                .active(false)
                .createdBy(SYSTEM)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        UserAuthProvider provider = UserAuthProvider.builder()
                .user(savedUser)
                .provider("GOOGLE")
                .providerUserId(googleSubject)
                .providerEmail(normalizedEmail)
                .createdBy(SYSTEM)
                .build();

        userAuthProviderRepository.saveAndFlush(provider);

        // Activate the account and initialize default data.
        entityManager
                .createStoredProcedureQuery("initialize_new_user")
                .registerStoredProcedureParameter(
                        1, Long.class, ParameterMode.IN
                )
                .setParameter(1, savedUser.getId())
                .execute();

        // Refresh fields updated by the stored procedure.
        entityManager.refresh(savedUser);

        return savedUser;
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
        log.info("Account activation request received");
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
