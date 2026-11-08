package com.pf.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserLogContextTests {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void addsSafeUserFieldsAndRestoresPreviousContext() {
        MDC.put("requestId", "request-1");

        try (UserLogContext ignored = UserLogContext.open(42L, "../alice")) {
            assertEquals("_alice", MDC.get("username"));
            assertEquals("42", MDC.get("userId"));
            assertEquals("_alice", MDC.get("userLogFile"));
        }

        assertEquals("request-1", MDC.get("requestId"));
        assertNull(MDC.get("userLogFile"));
    }
}
