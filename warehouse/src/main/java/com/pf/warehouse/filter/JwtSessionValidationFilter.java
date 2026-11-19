package com.pf.warehouse.filter;

import com.pf.common.logging.UserLogContext;
import com.pf.common.service.redis.RedisSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSessionValidationFilter extends OncePerRequestFilter {
    private final RedisSessionService redisSessionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        Object userIdClaim = jwtAuth.getToken().getClaim("userId");
        Long userId = userIdClaim instanceof Number number ? number.longValue() : null;
        String username = jwtAuth.getToken().getClaimAsString("username");
        try (UserLogContext ignored = UserLogContext.open(userId, username)) {
            validateSession(request, response, filterChain, jwtAuth);
        }
    }

    private void validateSession(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            JwtAuthenticationToken jwtAuth
    ) throws ServletException, IOException {
        if ("/api/auth/logout".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionId = jwtAuth.getToken().getClaimAsString("sid");
        String tokenId = jwtAuth.getToken().getId();
        if (sessionId == null || tokenId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            log.info("Rejected invalid authentication token");
            return;
        }

        String currentTokenId;
        try {
            currentTokenId = redisSessionService.getCurrentTokenId(sessionId);
        } catch (DataAccessException ex) {
            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Redis Authentication service temporarily unavailable");
            log.info("Redis authentication service temporarily unavailable");
            return;
        }

        if (!tokenId.equals(currentTokenId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired or superseded");
            log.info("Rejected expired or superseded authentication token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
