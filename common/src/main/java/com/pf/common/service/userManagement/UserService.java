package com.pf.common.service.userManagement;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.dto.userManagement.UserPermissionUpdateRequest;
import com.pf.common.dto.userManagement.UserPermissionViewDto;
import com.pf.common.entity.userManagement.ProfilePicture;
import com.pf.common.entity.userManagement.User;
import com.pf.common.entity.userManagement.UserPasswordHistory;
import com.pf.common.exception.ResourceNotFoundException;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.mapper.userManagement.UserPermissionViewMapper;
import com.pf.common.repository.user.ProfilePictureRepository;
import com.pf.common.repository.user.UserPermissionRepository;
import com.pf.common.repository.user.UserPermissionViewRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.PasswordUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pf.common.constants.EntityConstants.ADMIN_USER;
import static com.pf.common.constants.FieldConstants.ADMIN_USER_ID;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.SortOrder.ASC;
import static com.pf.common.util.BatchUtils.processBatchesAndSum;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends BaseService {
    private final UserMapper userMapper;
    private final UserPermissionViewMapper userPermissionViewMapper;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserPermissionViewRepository userPermissionViewRepository;
    private final ProfilePictureRepository profilePictureRepository;
    private final ProfilePictureStorageService profilePictureStorageService;
    private final UserPasswordHistoryService userPasswordHistoryService;
    private final EntityManager entityManager;

    @Transactional
    public GridResult fetchUsersGridData(SearchCriteria searchCriteria) {
        addAdminUserGridFilter(searchCriteria, ADMIN_USER_ID);
        log.info("Fetching users grid data: searchCriteria={}", searchCriteria);
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
        log.info("Fetched users grid data: totalRecords={}", totalRecords);
        return gridResult(totalRecords, recordDetails);
    }

    public List<UserPermissionViewDto> fetchUserPermissions(Long userId) {
        if (!userRepository.existsByIdAndAdminUserId(userId, fetchAdminUserId())) {
            throw new ResourceNotFoundException("User not found");
        }
        return fetchPermissionsForUser(userId);
    }

    public List<UserPermissionViewDto> fetchPermissionsForUser(Long userId) {
        return userPermissionViewMapper.toDtoList(
                userPermissionViewRepository
                        .findAllByUserIdOrderByModuleDisplayOrderAscScreenDisplayOrderAscActionDisplayOrderAsc(
                                userId
                        )
        );
    }

    @Transactional
    public ApiResponse updateUserPermissions(UserPermissionUpdateRequest request) {
        if (request.getUserId() == null ||
                !userRepository.existsByIdAndAdminUserId(request.getUserId(), fetchAdminUserId())) {
            return failure("User not found");
        }
        if (userRepository.existsAdminUserById(request.getUserId())) {
            return failure("The admin user already has permissions to all actions");
        }
        if (request.getPermissions() == null) {
            return failure("Permission updates are required");
        }
        if (request.getPermissions().stream().anyMatch(permission ->
                permission == null || permission.getId() == null || permission.getHasPermission() == null
        )) {
            return failure("Each permission update must include an ID and permission value");
        }
        if (request.getPermissions().stream()
                .map(UserPermissionUpdateRequest.UserPermissionUpdate::getId)
                .distinct()
                .count() != request.getPermissions().size()) {
            return failure("Permission updates must not contain duplicate IDs");
        }

        Long updatedBy = fetchLoginUserId();
        LocalDateTime updatedAt = LocalDateTime.now();
        Map<Boolean, List<Long>> permissionIdsByValue = request.getPermissions().stream()
                .collect(Collectors.groupingBy(
                        UserPermissionUpdateRequest.UserPermissionUpdate::getHasPermission,
                        Collectors.mapping(UserPermissionUpdateRequest.UserPermissionUpdate::getId,
                                Collectors.toList())
                ));
        for (Map.Entry<Boolean, List<Long>> entry : permissionIdsByValue.entrySet()) {
            int updated = processBatchesAndSum(
                    entry.getValue(),
                    permissionIds -> userPermissionRepository.updatePermissions(
                            permissionIds,
                            request.getUserId(),
                            entry.getKey(),
                            updatedBy,
                            updatedAt
                    )
            );
            if (updated != entry.getValue().size()) {
                throw new IllegalArgumentException(
                        "One or more permissions do not belong to user " + request.getUserId()
                );
            }
        }
        return success("User permissions have been updated successfully");
    }

    @Transactional
    public ApiResponse saveUser(UserDto dto) {
        log.info("Saving user");
        if (dto.getId() == null) {
            log.info("Creating new user");
            if (userRepository.existsByUsername(dto.getUsername().toLowerCase(Locale.ROOT).trim())) {
                return failure("Username is already in use");
            }
            User user = userMapper.toEntity(dto);
            user.setPasswordHash(PasswordUtils.encode(dto.getPassword()));
            user.setAdminUser(fetchAdminUser());
            user.setIsAdminUser(Boolean.FALSE);
            user.setActive(Boolean.TRUE);
            user.setCreatedBy(fetchLoginUsername());
            User savedUser = userRepository.saveAndFlush(user);
            initializeUserPermissions(savedUser.getId());
            userPasswordHistoryService.updatePasswordHistory(
                    savedUser,
                    UserPasswordHistory.PasswordActionType.INITIAL
            );
            log.info("New user created");
        } else {
            User user = userRepository.findById(dto.getId()).orElse(null);
            if (user == null) {
                return failure("User not found");
            }
            log.info("Updating existing user");
            user.setDisplayName(dto.getDisplayName());
            user.setEmail(dto.getEmail());
            user.setUpdatedBy(fetchLoginUsername());
            userRepository.save(user);
            log.info("Updated existing user");
        }
        return success("User has been saved successfully");
    }

    private void initializeUserPermissions(Long userId) {
        entityManager
                .createStoredProcedureQuery("initialize_user_permissions")
                .registerStoredProcedureParameter(1, Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, Long.class, ParameterMode.IN)
                .setParameter(1, userId)
                .setParameter(2, fetchLoginUserId())
                .execute();
    }

    @Transactional
    public ApiResponse activateUsers(List<Long> ids) {
        log.info("Activating users: requestedCount={}", ids == null ? 0 : ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        String updatedBy = fetchLoginUsername();
        int count = processBatchesAndSum(
                ids,
                chunk -> userRepository.activateUsers(chunk, updatedBy, updatedTime)
        );
        log.info("Activated users: count={}", count);
        return success("User(s) have been activated successfully");
    }

    @Transactional
    public ApiResponse inactivateUsers(List<Long> ids) {
        log.info("Inactivating users: requestedCount={}", ids == null ? 0 : ids.size());
        LocalDateTime updatedTime = LocalDateTime.now();
        String updatedBy = fetchLoginUsername();
        int count = processBatchesAndSum(
                ids,
                chunk -> userRepository.inactivateUsers(chunk, updatedBy, updatedTime)
        );
        log.info("Inactivated users: count={}", count);
        return success("User(s) have been inactivated successfully");
    }

    @Transactional
    public ApiResponse deleteUsers(List<Long> ids) {
        try {
            log.info("Deleting users: requestedCount={}", ids == null ? 0 : ids.size());
            LocalDateTime deletedAT = LocalDateTime.now();
            String deletedBy = fetchLoginUsername();
            int count = processBatchesAndSum(
                    ids,
                    chunk -> userRepository.deleteUsers(chunk, deletedBy, deletedAT)
            );
            userRepository.flush();
            log.info("Deleted users: count={}", count);
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
            log.error("Failed to save profile picture", e);
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

}
