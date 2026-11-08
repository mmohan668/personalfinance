package com.pf.common.logging;

import org.slf4j.MDC;

import java.util.Map;

public final class UserLogContext implements AutoCloseable {
    private final Map<String, String> previousContext;

    private UserLogContext(Long userId, String username) {
        previousContext = MDC.getCopyOfContextMap();
        String safeUsername = sanitize(username);
        String id = userId == null ? "unknown" : userId.toString();
        MDC.put("username", safeUsername);
        MDC.put("userId", id);
        MDC.put("userLogFile", safeUsername);
    }

    public static UserLogContext open(Long userId, String username) {
        return new UserLogContext(userId, username);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("^\\.+", "");
        if (sanitized.isBlank()) {
            return "unknown";
        }
        return sanitized.substring(0, Math.min(sanitized.length(), 64));
    }

    @Override
    public void close() {
        MDC.clear();
        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        }
    }
}
