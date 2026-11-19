package com.pf.scheduler.recurring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Slf4j
public class FinancialReportScheduler {
    private final AsyncFinancialReportClient client;
    private final ZoneId timeZone;

    public FinancialReportScheduler(AsyncFinancialReportClient client,
                                    @Value("${scheduler.financial-report-time-zone:Asia/Kolkata}") String timeZone) {
        this.client = client;
        this.timeZone = ZoneId.of(timeZone);
    }

    @Scheduled(cron = "${scheduler.financial-report-cron:0 0 9 * * *}", zone = "${scheduler.financial-report-time-zone:Asia/Kolkata}")
    public void dispatchReports() {
        try {
            client.dispatch(LocalDate.now(timeZone));
        } catch (RuntimeException exception) {
            log.error("Financial report dispatch failed", exception);
        }
    }
}
