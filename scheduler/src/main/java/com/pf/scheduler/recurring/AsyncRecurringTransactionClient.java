package com.pf.scheduler.recurring;

import com.pf.common.dto.recurringTransaction.SchedulerRecurringTransactionRuleDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class AsyncRecurringTransactionClient {
    private final RestClient restClient;
    private final String schedulerApiToken;

    public AsyncRecurringTransactionClient(
            @Value("${app.urls.async-rest-url}") String asyncBaseUrl,
            @Value("${async.scheduler-token:}") String schedulerApiToken
    ) {
        this.restClient = RestClient.create(asyncBaseUrl);
        this.schedulerApiToken = schedulerApiToken;
    }

    public List<SchedulerRecurringTransactionRuleDto> fetchRules() {
        requireToken();
        List<SchedulerRecurringTransactionRuleDto> rules = restClient.get()
                .uri("/api/recurringTransactions/scheduler/rules")
                .header("X-Async-Scheduler-Token", schedulerApiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (rules == null) throw new IllegalStateException("Async returned an empty recurring rules response");
        return rules;
    }

    public boolean execute(Long ruleId, LocalDate occurrenceDate, LocalTime occurrenceTime) {
        requireToken();
        Boolean executed = restClient.post()
                .uri("/api/recurringTransactions/scheduler/execute/{ruleId}/{date}/{time}",
                        ruleId, occurrenceDate, occurrenceTime)
                .header("X-Async-Scheduler-Token", schedulerApiToken)
                .retrieve()
                .body(Boolean.class);
        if (executed == null) throw new IllegalStateException("Async returned no recurring execution result");
        return executed;
    }

    private void requireToken() {
        if (schedulerApiToken.isBlank()) {
            throw new IllegalStateException("async.scheduler-token must be configured");
        }
    }
}
