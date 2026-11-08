package com.pf.flyway.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogControl implements ApplicationListener<ApplicationReadyEvent> {
    private static volatile boolean applicationReady;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        applicationReady = true;
    }

    public static class StartupOnlyFilter extends Filter<ILoggingEvent> {
        @Override
        public FilterReply decide(ILoggingEvent event) {
            return applicationReady ? FilterReply.DENY : FilterReply.NEUTRAL;
        }
    }

    public static class RuntimeOnlyFilter extends Filter<ILoggingEvent> {
        @Override
        public FilterReply decide(ILoggingEvent event) {
            return applicationReady ? FilterReply.NEUTRAL : FilterReply.DENY;
        }
    }
}
