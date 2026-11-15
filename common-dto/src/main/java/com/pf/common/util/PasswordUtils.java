package com.pf.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final BCryptPasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder(12);

    private PasswordUtils() {
    }

    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    public static String generateEncodedRandomPassword() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawPassword = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        return PASSWORD_ENCODER.encode(rawPassword);
    }
}
