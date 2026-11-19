package com.pf.scheduler.recurring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
public class AsyncFinancialReportClient {
    private final RestClient restClient;
    private final String schedulerApiToken;

    public AsyncFinancialReportClient(
            @Value("${app.urls.async-rest-url}") String asyncBaseUrl,
            @Value("${async.scheduler-token:}") String schedulerApiToken) {
        this.restClient = RestClient.create(asyncBaseUrl);
        this.schedulerApiToken = schedulerApiToken;
    }

    public void dispatch(LocalDate today) {
        if (schedulerApiToken.isBlank()) throw new IllegalStateException("async.scheduler-token must be configured");
        restClient.post().uri("/api/reports/scheduler/dispatch/{today}", today)
                .header("X-Async-Scheduler-Token", schedulerApiToken).retrieve().toBodilessEntity();
    }
}
