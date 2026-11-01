package com.pf.common.service.userManagement;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.ChangePassword;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.ProfilePicture;
import com.pf.common.entity.userManagement.User;
import com.pf.common.entity.userManagement.UserPasswordHistory;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.repository.user.ProfilePictureRepository;
import com.pf.common.repository.user.UserPasswordHistoryRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.BatchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static com.pf.common.constants.EntityConstants.ADMIN_USER;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.SortOrder.ASC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends BaseService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ProfilePictureRepository profilePictureRepository;
    private final ProfilePictureStorageService profilePictureStorageService;
    private final UserPasswordHistoryRepository userPasswordHistoryRepository;
    private final static int passwordsCount = 5;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GridResult fetchUsersGridData(SearchCriteria searchCriteria) {
        log.debug("fetchUsersGridData: {}", searchCriteria);
        searchCriteria.getSortList().add(new GridSort(ID, ASC.getValue()));
        searchCriteria.setFetchPaths(List.of(ADMIN_USER));
        searchCriteria.setFIELD_MAPPINGS(UserDto.FIELD_MAPPINGS);
        long totalRecords = 0;
        if (!searchCriteria.isLoadAllData()) {
            totalRecords = getCountBySearchCriteria(
                    User.class,
                    searchCriteria
            );
            if (totalRecords == 0) {
                return gridResult(totalRecords, List.of());
            }
        }
        List<UserDto> recordDetails = userMapper.toDtoList(
                getDataBySearchCriteria(User.class, searchCriteria)
        );
        log.debug("fetchUsersGridData: totalRecords: {}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    @Transactional
    public ApiResponse saveUser(UserDto dto) {
        log.debug("saveUser: username {}", dto.getUsername());
        if (dto.getId() == null) {
            log.debug("Creating new user {}", dto.getUsername());
            if (userRepository.existsByUsername(dto.getUsername().toLowerCase(Locale.ROOT).trim())) {
                return failure("Username is already in use");
            }
            User user = userMapper.toEntity(dto);
            user.setAdminUser(fetchAdminUser());
            user.setIsAdminUser(Boolean.FALSE);
            user.setCreatedBy(fetchLoginUsername());
            userRepository.save(user);
            log.debug("New user created {}", user.getUsername());
        } else {
            User user = userRepository.findById(dto.getId()).orElse(null);
            if (user == null) {
                return failure("User not found");
            }
            log.debug("Updating user {}", user.getUsername());
            user.setDisplayName(dto.getDisplayName());
            user.setEmail(dto.getEmail());
            user.setUpdatedBy(fetchLoginUsername());
            userRepository.save(user);
            log.debug("Updated user {}", user.getUsername());
        }
        return success("User has been saved successfully");
    }

    @Transactional
    public ApiResponse activateUsers(List<Long> ids) {
        log.debug("activateUsers: ids {}", ids);
        LocalDateTime updatedTime = LocalDateTime.now();
        String updatedBy = fetchLoginUsername();
        int count = BatchUtils.processBatchesAndSum(
                ids,
                chunk -> userRepository.activateUsers(chunk, updatedBy, updatedTime)
        );
        log.debug("activated User(s): count {}", count);
        return success("User(s) have been activated successfully");
    }

    @Transactional
    public ApiResponse inactivateUsers(List<Long> ids) {
        log.debug("inactivateUsers: ids {}", ids);
        LocalDateTime updatedTime = LocalDateTime.now();
        String updatedBy = fetchLoginUsername();
        int count = BatchUtils.processBatchesAndSum(
                ids,
                chunk -> userRepository.inactivateUsers(chunk, updatedBy, updatedTime)
        );
        log.debug("inactivated User(s): count {}", count);
        return success("User(s) have been inactivated successfully");
    }

    @Transactional
    public ApiResponse deleteUsers(List<Long> ids) {
        try {
            log.debug("deleteUsers: ids {}", ids);
            BatchUtils.processInBatches(ids, userRepository::deleteAllByIdInBatch);
            userRepository.flush();
            return success("User(s) have been deleted successfully");
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Failed to delete users due to a constraint violation {}", e.getMessage());
            return failure(
                    "Cannot delete selected users because one or more are referenced by other records."
            );
        }
    }

    @Transactional
    public ApiResponse saveProfilePicture(MultipartFile file) {
        String username = fetchLoginUsername();

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return failure("User not found");
        }

        String filename;

        try {
            filename = profilePictureStorageService.saveImage(fetchLoginUsername(), file);
        } catch (IllegalArgumentException e) {
            return failure(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to save profile picture for user {}", username, e);
            return failure("Failed to save profile picture");
        }

        ProfilePicture picture = profilePictureRepository
                .findByUserId(user.getId())
                .orElseGet(ProfilePicture::new);

        picture.setUser(user);
        picture.setImagePath(filename);
        picture.setContentType(file.getContentType());
        picture.setFileSize(file.getSize());

        profilePictureRepository.save(picture);

        return success("Profile picture saved successfully");
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
        if (!passwordEncoder.matches(
                changePassword.getCurrentPassword(),
                user.getPasswordHash())) {
            return failure("Current password is incorrect");
        }

        // Prevent reusing recent passwords
        List<UserPasswordHistory> passwordHistoryList =
                userPasswordHistoryRepository
                        .findByUser_IdOrderByChangedAtDesc(
                                changePassword.getUserId(),
                                PageRequest.of(0, passwordsCount));

        if (passwordHistoryList.stream().anyMatch(
                p -> passwordEncoder.matches(
                        changePassword.getNewPassword(),
                        p.getPasswordHash()))) {
            return failure(
                    "New password should be different from the last "
                            + passwordsCount + " passwords");
        }

        // Encode once and reuse the encoded value
        String encodedPassword =
                passwordEncoder.encode(changePassword.getNewPassword());

        user.setPasswordHash(encodedPassword);
        user.setUpdatedBy(fetchLoginUsername());
        userRepository.save(user);

        UserPasswordHistory history = new UserPasswordHistory();
        history.setUser(user);
        history.setPasswordHash(encodedPassword);
        history.setActionType(
                UserPasswordHistory.PasswordActionType.CHANGE);
        history.setChangedAt(Instant.now());
        history.setChangedBy(fetchLoginUsername());

        userPasswordHistoryRepository.save(history);

        return success("Password has been changed successfully. Please login again.");
    }

}
