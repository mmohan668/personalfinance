package com.pf.common.service.userManagement;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.userManagement.ChangePassword;
import com.pf.common.entity.userManagement.User;
import com.pf.common.entity.userManagement.UserPasswordHistory;
import com.pf.common.exception.AuthenticationFailedException;
import com.pf.common.repository.user.UserPasswordHistoryRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "warehouse"
)
@RequiredArgsConstructor
public class AuthenticationService extends BaseService {

    private final UserRepository userRepository;
    private final UserPasswordHistoryService userPasswordHistoryService;
    private final UserPasswordHistoryRepository userPasswordHistoryRepository;
    @Value("${app.password.history-limit:5}")
    private int passwordHistoryLimit;

    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new AuthenticationFailedException("Invalid username or password"));

        log.info("Password matches stored hash: {}",
                PasswordUtils.matches(rawPassword, user.getPasswordHash()));

        if (!PasswordUtils.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationFailedException(
                    "Account is not activated. Please activate your account."
            );
        }

        if (isPasswordExpired(user.getId())) {
            throw new AuthenticationFailedException(
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

    @Transactional
    public ApiResponse changePassword(ChangePassword changePassword) {

        if (changePassword.getUserId() == null) {
            return failure("User ID is required");
        }

        if (changePassword.getCurrentPassword() == null
                || changePassword.getCurrentPassword().isBlank()) {
            return failure("Current password is required");
        }

        if (changePassword.getNewPassword() == null
                || changePassword.getNewPassword().isBlank()) {
            return failure("New password is required");
        }

        User user = userRepository.findById(changePassword.getUserId())
                .orElse(null);

        if (user == null) {
            return failure("User not found");
        }

        // Verify the current password
        if (!PasswordUtils.matches(
                changePassword.getCurrentPassword(),
                user.getPasswordHash())) {
            return failure("Current password is incorrect");
        }

        // Prevent reusing recent passwords
        List<UserPasswordHistory> passwordHistoryList =
                userPasswordHistoryRepository
                        .findByUser_IdOrderByChangedAtDesc(
                                changePassword.getUserId(),
                                PageRequest.of(0, passwordHistoryLimit));

        if (passwordHistoryList.stream().anyMatch(
                p -> PasswordUtils.matches(
                        changePassword.getNewPassword(),
                        p.getPasswordHash()))) {
            return failure(
                    "New password should be different from the last "
                            + passwordHistoryLimit + " passwords");
        }

        // Encode once and reuse the encoded value
        String encodedPassword =
                PasswordUtils.encode(changePassword.getNewPassword());

        user.setPasswordHash(encodedPassword);
        user.setUpdatedBy(fetchLoginUsername());
        user.setMustChangePassword(false);
        userRepository.save(user);
        userPasswordHistoryService.updatePasswordHistory(
                user,
                UserPasswordHistory.PasswordActionType.CHANGE
        );
        return success("Password has been changed successfully. Please login again.");
    }
}
