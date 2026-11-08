package com.pf.scheduler.recurring;

import com.pf.common.dto.recurringTransaction.SchedulerRecurringTransactionRuleDto;
import com.pf.scheduler.holiday.PublicHolidayClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;

class RecurringTransactionSchedulerTests {
    private final AsyncRecurringTransactionClient client = mock(AsyncRecurringTransactionClient.class);
    private final PublicHolidayClient holidays = mock(PublicHolidayClient.class);
    private final RecurringTransactionScheduler scheduler =
            new RecurringTransactionScheduler(client, holidays);

    @Test
    void catchesUpEveryMissedDailyOccurrenceThroughAsyncApi() {
        SchedulerRecurringTransactionRuleDto rule = rule(LocalDate.of(2026, 11, 5), null, "DAILY", 1);
        when(client.fetchRules()).thenReturn(List.of(rule));
        when(client.execute(eq(1L), any(), any())).thenReturn(true);

        scheduler.processDueTransactions(Instant.parse("2026-11-08T10:00:00Z"));

        verify(client).execute(1L, LocalDate.of(2026, 11, 5), LocalTime.of(6, 0));
        verify(client).execute(1L, LocalDate.of(2026, 11, 6), LocalTime.of(6, 0));
        verify(client).execute(1L, LocalDate.of(2026, 11, 7), LocalTime.of(6, 0));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 0));
        verify(client).fetchRules();
    }

    @Test
    void doesNotExecuteTodayBeforeConfiguredLocalTime() {
        SchedulerRecurringTransactionRuleDto rule = rule(LocalDate.of(2026, 11, 8), null, "DAILY", 1);
        when(client.fetchRules()).thenReturn(List.of(rule));

        scheduler.processDueTransactions(Instant.parse("2026-11-08T00:00:00Z"));

        verify(client).fetchRules();
        verify(client, never()).execute(anyLong(), any(), any());
    }

    @Test
    void startsAfterResumeDateInsteadOfCatchingUpPausedOccurrences() {
        SchedulerRecurringTransactionRuleDto rule = rule(LocalDate.of(2026, 11, 5),
                LocalDateTime.of(2026, 11, 7, 6, 0), "DAILY", 1);
        when(client.fetchRules()).thenReturn(List.of(rule));
        when(client.execute(eq(1L), any(), any())).thenReturn(true);

        scheduler.processDueTransactions(Instant.parse("2026-11-08T10:00:00Z"));

        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 0));
        verify(client, never()).execute(eq(1L), eq(LocalDate.of(2026, 11, 6)), any());
        verify(client, never()).execute(eq(1L), eq(LocalDate.of(2026, 11, 7)), any());
    }

    @Test
    void catchesUpHourlyOccurrencesUsingStartTimeAsAnchor() {
        SchedulerRecurringTransactionRuleDto rule = rule(LocalDate.of(2026, 11, 8), null, "HOURLY", 2);
        when(client.fetchRules()).thenReturn(List.of(rule));
        when(client.execute(eq(1L), any(), any())).thenReturn(true);

        scheduler.processDueTransactions(Instant.parse("2026-11-08T04:30:00Z"));

        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 0));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(8, 0));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(10, 0));
        verify(client, never()).execute(eq(1L), any(), eq(LocalTime.of(12, 0)));
    }

    @Test
    void catchesUpMinuteOccurrencesAndSkipsProcessedIntervals() {
        SchedulerRecurringTransactionRuleDto rule = rule(LocalDate.of(2026, 11, 8),
                LocalDateTime.of(2026, 11, 8, 6, 2), "MINUTELY", 1);
        when(client.fetchRules()).thenReturn(List.of(rule));
        when(client.execute(eq(1L), any(), any())).thenReturn(true);

        scheduler.processDueTransactions(Instant.parse("2026-11-08T00:35:00Z"));

        verify(client, never()).execute(eq(1L), any(), eq(LocalTime.of(6, 2)));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 3));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 4));
        verify(client).execute(1L, LocalDate.of(2026, 11, 8), LocalTime.of(6, 5));
    }

    private static SchedulerRecurringTransactionRuleDto rule(
            LocalDate start, LocalDateTime lastExecuted, String frequency, int interval
    ) {
        return SchedulerRecurringTransactionRuleDto.builder()
                .id(1L)
                .adminUserId(1L)
                .amount(BigDecimal.TEN)
                .frequency(frequency)
                .intervalValue(interval)
                .startDate(start)
                .executionTime(LocalTime.of(6, 0))
                .timeZone("Asia/Kolkata")
                .countryCode("IN")
                .countryName("India")
                .stateCode("IN-KA")
                .stateName("Karnataka")
                .lastExecutedAt(lastExecuted)
                .build();
    }
}
