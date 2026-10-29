
package com.pf.common.service.userManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@ConditionalOnProperty(
        name = "spring.application.name",
        havingValue = "warehouse"
)
public class ActivationTokenService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;
    private static final long EXPIRY_MILLIS = 24L * 60 * 60 * 1000;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public ActivationTokenService(
            @Value("${app.activation-token.secret}") String secret) {

        byte[] key;

        try {
            key = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Activation token secret must be valid Base64", e);
        }

        if (key.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Activation token secret must be a Base64-encoded 32-byte key");
        }

        this.secretKey = new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * Generates a URL-safe activation token valid for 24 hours.
     */
    public String generateToken(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            long expiresAt = System.currentTimeMillis() + EXPIRY_MILLIS;

            byte[] payload = ByteBuffer.allocate(16)
                    .putLong(userId)
                    .putLong(expiresAt)
                    .array();

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            byte[] encryptedPayload = cipher.doFinal(payload);

            byte[] tokenBytes = ByteBuffer
                    .allocate(iv.length + encryptedPayload.length)
                    .put(iv)
                    .put(encryptedPayload)
                    .array();

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(tokenBytes);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unable to generate activation token", e);
        }
    }

    /**
     * Decrypts and validates the token.
     * Returns the user ID if valid; otherwise throws an exception.
     */
    public Long validateAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Activation token is missing.");
        }

        final byte[] tokenBytes;
        try {
            tokenBytes = Base64.getUrlDecoder().decode(token);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid activation link.");
        }

        if (tokenBytes.length != IV_LENGTH + 16 + 16) {
            throw new IllegalArgumentException("Invalid activation link.");
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedPayload = new byte[tokenBytes.length - IV_LENGTH];

            System.arraycopy(tokenBytes, 0, iv, 0, IV_LENGTH);
            System.arraycopy(
                    tokenBytes, IV_LENGTH,
                    encryptedPayload, 0,
                    encryptedPayload.length
            );

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            byte[] payload = cipher.doFinal(encryptedPayload);

            if (payload.length != 16) {
                throw new IllegalArgumentException("Invalid activation link.");
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            long userId = buffer.getLong();
            long expiresAt = buffer.getLong();

            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid activation link.");
            }

            if (System.currentTimeMillis() >= expiresAt) {
                throw new IllegalArgumentException(
                        "Activation link has expired. Please sign up again."
                );
            }

            return userId;

        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(
                    "Invalid activation link. It may have been modified or is no longer valid."
            );
        }
    }
}
