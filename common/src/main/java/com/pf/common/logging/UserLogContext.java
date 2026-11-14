package com.pf.common.logging;

import org.slf4j.MDC;

import java.util.Map;

public final class UserLogContext implements AutoCloseable {
    public static final String UNKNOWN = "unknown";
    public static final String USERNAME = "username";
    public static final String USER_ID = "userId";
    public static final String USER_LOG_FILE = "userLogFile";

    private final Map<String, String> previousContext;

    private UserLogContext(Long userId, String username) {
        previousContext = MDC.getCopyOfContextMap();
        String safeUsername = sanitize(username);
        String id = userId == null ? UNKNOWN : userId.toString();
        MDC.put(USERNAME, safeUsername);
        MDC.put(USER_ID, id);
        MDC.put(USER_LOG_FILE, safeUsername);
    }

    public static UserLogContext open(Long userId, String username) {
        return new UserLogContext(userId, username);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("^\\.+", "");
        if (sanitized.isBlank()) {
            return UNKNOWN;
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
