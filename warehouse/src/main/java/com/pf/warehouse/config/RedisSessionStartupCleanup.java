package com.pf.warehouse.config;

import com.pf.common.service.redis.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSessionStartupCleanup implements ApplicationRunner {
    private final RedisSessionService redisSessionService;

    @Override
    public void run(ApplicationArguments args) {
        long deletedSessions = redisSessionService.deleteAllSessions();
        log.info("Cleared {} JWT session(s) from Redis at Warehouse startup", deletedSessions);
    }
}
