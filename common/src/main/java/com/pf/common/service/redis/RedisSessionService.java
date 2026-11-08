package com.pf.common.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(15);
    private static final String SESSION_KEY_PREFIX = "jwt:session:";
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final Duration CAPTCHA_TIMEOUT = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    public void setCurrentTokenId(String sessionId, String tokenId) {
        redisTemplate.opsForValue().set(
                sessionKey(sessionId),
                tokenId,
                SESSION_TIMEOUT
        );
    }

    public String getCurrentTokenId(String sessionId) {
        return redisTemplate.opsForValue().get(
                sessionKey(sessionId)
        );
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(sessionKey(sessionId));
    }

    public long deleteAllSessions() {
        Long deleted = redisTemplate.execute((RedisCallback<Long>) connection -> {
            long deletedCount = 0;
            ScanOptions options = ScanOptions.scanOptions()
                    .match(SESSION_KEY_PREFIX + "*")
                    .count(500)
                    .build();
            try (Cursor<byte[]> keys = connection.keyCommands().scan(options)) {
                while (keys.hasNext()) {
                    deletedCount += connection.keyCommands().del(keys.next());
                }
            }
            return deletedCount;
        });
        return Objects.requireNonNull(deleted, "Redis did not return the deleted session count");
    }


    public boolean rotateTokenId(
            String sessionId,
            String expectedTokenId,
            String newTokenId) {

        String script = """
                local current = redis.call('GET', KEYS[1])
                if current ~= ARGV[1] then
                    return 0
                end
                redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                return 1
                """;

        DefaultRedisScript<Long> redisScript =
                new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                List.of(sessionKey(sessionId)),
                expectedTokenId,
                newTokenId,
                String.valueOf(SESSION_TIMEOUT.toSeconds())
        );

        return Long.valueOf(1L).equals(result);
    }

    public void saveCaptcha(String captchaId, String answer) {
        redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaId,
                answer,
                CAPTCHA_TIMEOUT
        );
    }

    public String getCaptchaAnswer(String captchaId) {
        return redisTemplate.opsForValue().get(
                CAPTCHA_KEY_PREFIX + captchaId
        );
    }

    public void deleteCaptcha(String captchaId) {
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + captchaId);
    }

}
