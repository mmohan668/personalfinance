package com.pf.common.service.userManagement;

import com.pf.common.entity.userManagement.User;
import com.pf.common.repository.user.UserPasswordHistoryRepository;
import com.pf.common.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "warehouse"
)
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPasswordHistoryRepository userPasswordHistoryRepository;

    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid username or password"));

        log.info("Password matches stored hash: {}",
                passwordEncoder.matches(rawPassword, user.getPasswordHash()));

        System.out.println("Password matches stored hash: "
                + passwordEncoder.matches(rawPassword, user.getPasswordHash()));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException(
                    "Account is not activated. Please activate your account."
            );
        }

        if (isPasswordExpired(user.getId())) {
            throw new IllegalStateException(
                    "Password expired. Please reset your password."
            );
        }

        return user;
    }

    private boolean isPasswordExpired(Long userId) {
        Instant lastChangedAt = userPasswordHistoryRepository
                .findLastChangedAtByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Password history not found for user"
                ));

        return !lastChangedAt.isAfter(
                Instant.now().minus(90, ChronoUnit.DAYS)
        );
    }
}
