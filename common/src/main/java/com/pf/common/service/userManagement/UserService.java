package com.pf.common.service.userManagement;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.User;
import com.pf.common.mapper.userManagement.UserMapper;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.util.BatchUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static com.pf.common.constants.CommonConstants.SYSTEM;
import static com.pf.common.constants.EntityConstants.ADMIN_USER;
import static com.pf.common.constants.FieldConstants.ID;
import static com.pf.common.enums.SortOrder.ASC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends BaseService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

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
            user.setAdminUser(fetchLoginUser().getAdminUser());
            user.setIsAdminUser(Boolean.FALSE);
            user.setCreatedBy(fetchLoginUser().getUsername());
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
            user.setUpdatedBy(fetchLoginUser().getUsername());
            userRepository.save(user);
            log.debug("Updated user {}", user.getUsername());
        }
        return success("User has been saved successfully");
    }

    @Transactional
    public ApiResponse activateUsers(List<Long> ids) {
        log.debug("activateUsers: ids {}", ids);
        LocalDateTime updatedTime = LocalDateTime.now();
        String updatedBy = fetchLoginUser().getUsername();
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
        String updatedBy = fetchLoginUser().getUsername();
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
        user.setIsAdminUser(Boolean.TRUE);
        user.setCreatedBy(SYSTEM);

        User savedUser = userRepository.save(user);
        activateAccount(savedUser.getId());
        log.info("User account created successfully. Awaiting email activation for username: {}", username);

        return success("Account created successfully! Please check your email to activate your account.");
    }

    private void activateAccount(Long id) {
        log.info("Starting account activation and initialization for user ID: {}", id);

        try {
            entityManager
                    .createStoredProcedureQuery("initialize_new_user")
                    .registerStoredProcedureParameter(
                            1, Long.class, ParameterMode.IN)
                    .setParameter(1, id)
                    .execute();

            log.info(
                    "Account activated and initialized successfully for user ID: {}",
                    id
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to activate and initialize account for user ID: {}",
                    id,
                    ex
            );

            throw ex;
        }
    }
}
