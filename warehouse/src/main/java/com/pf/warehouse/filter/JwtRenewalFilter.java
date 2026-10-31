
package com.pf.warehouse.filter;

import com.pf.warehouse.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRenewalFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtRenewalFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        // Renew only successful responses.
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (response.isCommitted()
                || authentication == null
                || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return;
        }

        Object adminUserIdClaim =
                jwtAuth.getToken().getClaim("adminUserId");

        if (!(adminUserIdClaim instanceof Number adminUserId)) {
            return;
        }

        String renewedToken = jwtService.generateToken(
                jwtAuth.getToken().getSubject(),
                jwtAuth.getToken().getClaimAsString("username"),
                Boolean.TRUE.equals(jwtAuth.getToken().getClaim("admin")),
                adminUserId.longValue()
        );

        response.setHeader("X-New-Access-Token", renewedToken);
    }
}
