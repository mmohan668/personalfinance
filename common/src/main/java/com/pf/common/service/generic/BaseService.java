package com.pf.common.service.generic;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.entity.userManagement.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseService extends GenericCriteriaService {

    @PersistenceContext
    private EntityManager entityManager;

    public ApiResponse success(String message) {
        return success(message, null);
    }

    public ApiResponse failure(String message) {
        return failure(message, null);
    }

    public ApiResponse failure(String message, Object data) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    public ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public GridResult gridResult(long totalRecords, List<?> recordDetails) {
        return GridResult.builder()
                .totalRecords(totalRecords)
                .recordDetails(recordDetails)
                .build();
    }

    public Long fetchLoginUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object claim = jwtAuth.getToken().getClaim("userId");

        if (!(claim instanceof Number userId)) {
            throw new RuntimeException("User not found in JWT");
        }

        return userId.longValue();
    }

    public User fetchLoginUser() {
        return entityManager.getReference(User.class, fetchLoginUserId());
    }

    public String fetchLoginUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object claim = jwtAuth.getToken().getClaim("username");

        if (!(claim instanceof String username)) {
            throw new RuntimeException("User ID not found in JWT");
        }

        return username;
    }

    public Long fetchAdminUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object claim = jwtAuth.getToken().getClaim("adminUserId");

        if (!(claim instanceof Number adminUserId)) {
            throw new RuntimeException("User ID not found in JWT");
        }

        return adminUserId.longValue();
    }

    public User fetchAdminUser() {
        return entityManager.getReference(User.class, fetchAdminUserId());
    }

}
