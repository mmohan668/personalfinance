
package com.pf.warehouse.filter;

import com.pf.common.record.jwt.GeneratedJwt;
import com.pf.common.service.redis.RedisSessionService;
import com.pf.warehouse.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class JwtRenewalFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;

    public JwtRenewalFilter(
            JwtService jwtService,
            RedisSessionService redisSessionService) {
        this.jwtService = jwtService;
        this.redisSessionService = redisSessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Do not renew the token during logout.
        if ("/api/auth/logout".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Validate the incoming token against Redis before the controller runs.
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionId = jwtAuth.getToken().getClaimAsString("sid");
        String tokenId = jwtAuth.getToken().getId();

        if (sessionId == null || tokenId == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token");
            log.debug("Invalid token");
            return;
        }

        String currentTokenId;

        try {
            currentTokenId =
                    redisSessionService.getCurrentTokenId(sessionId);
        } catch (DataAccessException ex) {
            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Redis Authentication service temporarily unavailable"
            );
            log.debug("Redis Authentication service temporarily unavailable");
            return;
        }

        if (!tokenId.equals(currentTokenId)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token expired or superseded");
            log.debug("Token expired or superseded");
            return;
        }

        // Execute the endpoint first.
        filterChain.doFilter(request, response);

        // Renew only successful responses that haven't been committed.
        if (response.getStatus() < 200
                || response.getStatus() >= 300
                || response.isCommitted()) {
            return;
        }

        Object adminUserIdClaim =
                jwtAuth.getToken().getClaim("adminUserId");

        if (!(adminUserIdClaim instanceof Number adminUserId)) {
            return;
        }

        GeneratedJwt generatedJwt = jwtService.generateToken(
                jwtAuth.getToken().getSubject(),
                jwtAuth.getToken().getClaimAsString("username"),
                Boolean.TRUE.equals(
                        jwtAuth.getToken().getClaim("admin")),
                adminUserId.longValue(),
                sessionId);

        // Store the new token ID and refresh the 15-minute idle timeout.
        boolean rotated;

        try {
            rotated = redisSessionService.rotateTokenId(
                    sessionId,
                    currentTokenId,
                    generatedJwt.tokenId()
            );
        } catch (DataAccessException ex) {
            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Authentication service temporarily unavailable"
                );
                log.debug("Authentication service temporarily unavailable");
            }
            return;
        }

        if (rotated) {
            response.setHeader(
                    "X-New-Access-Token",
                    generatedJwt.token());
        }

    }
}
