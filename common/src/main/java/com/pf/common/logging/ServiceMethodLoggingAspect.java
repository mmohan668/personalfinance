package com.pf.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceMethodLoggingAspect {

    @Around("@within(org.springframework.stereotype.Service) && execution(public * *(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.nanoTime();

        logger.info("Starting service action: {}", methodName);
        try {
            Object result = joinPoint.proceed();
            logger.info("Completed service action: {} ({} ms)",
                    methodName, elapsedMillis(startTime));
            return result;
        } catch (Throwable exception) {
            logger.error("Service action failed: {} ({} ms)",
                    methodName, elapsedMillis(startTime), exception);
            throw exception;
        }
    }

    private long elapsedMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
