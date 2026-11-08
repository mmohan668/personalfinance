package com.pf.scheduler.recurring;

import com.pf.common.dto.recurringTransaction.SchedulerRecurringTransactionRuleDto;
import com.pf.scheduler.holiday.PublicHolidayClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

@Component
@Slf4j
public class RecurringTransactionScheduler {
    private final AsyncRecurringTransactionClient client;
    private final PublicHolidayClient holidayClient;

    public RecurringTransactionScheduler(
            AsyncRecurringTransactionClient client,
            PublicHolidayClient holidayClient
    ) {
        this.client = client;
        this.holidayClient = holidayClient;
    }

    @Scheduled(fixedDelayString = "${scheduler.recurring-poll-delay-ms:60000}")
    public void processDueTransactions() {
        log.info("Starting recurring transaction scheduler poll");
        boolean completedSuccessfully = false;
        try {
            completedSuccessfully = processDueTransactions(Instant.now());
        } finally {
            log.info("Recurring transaction scheduler poll {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }

    boolean processDueTransactions(Instant now) {
        boolean completedSuccessfully = true;
        try {
            for (SchedulerRecurringTransactionRuleDto rule : client.fetchRules()) {
                try {
                    processRule(rule, now);
                } catch (RuntimeException exception) {
                    log.error("Failed to process recurring transaction rule {}", rule.getId(), exception);
                    completedSuccessfully = false;
                }
            }
        } catch (RuntimeException exception) {
            log.error("Unable to fetch recurring transaction rules from Async", exception);
            return false;
        }
        return completedSuccessfully;
    }

    private void processRule(SchedulerRecurringTransactionRuleDto rule, Instant now) {
        ZoneId zone = ZoneId.of(rule.getTimeZone());
        LocalDateTime localNow = now.atZone(zone).toLocalDateTime();
        LocalDate today = localNow.toLocalDate();
        LocalDateTime lastProcessedAt = latestProcessedAt(rule);
        if ("HOURLY".equals(rule.getFrequency()) || "MINUTELY".equals(rule.getFrequency())) {
            processSubdailyRule(rule, localNow, lastProcessedAt);
            return;
        }

        LocalDate dueThrough = rule.getEndDate() == null || rule.getEndDate().isAfter(today)
                ? today : rule.getEndDate();
        LocalDate firstDate = lastProcessedAt == null
                ? rule.getStartDate()
                : lastProcessedAt.toLocalDate().plusDays(
                        lastProcessedAt.toLocalTime().isBefore(rule.getExecutionTime()) ? 0 : 1
                );
        if (firstDate.isBefore(rule.getStartDate())) firstDate = rule.getStartDate();

        for (LocalDate date = firstDate; !date.isAfter(dueThrough); date = date.plusDays(1)) {
            if (!isOccurrence(rule, date)) continue;
            LocalDateTime occurrenceAt = LocalDateTime.of(date, rule.getExecutionTime());
            if (occurrenceAt.isAfter(localNow)) continue;
            if (client.execute(rule.getId(), date, rule.getExecutionTime())) {
                log.info("Processed recurring transaction occurrence: occurrence={}", occurrenceAt);
            } else {
                log.info("Recurring transaction occurrence already exists: occurrence={}", occurrenceAt);
            }
        }
    }

    private void processSubdailyRule(
            SchedulerRecurringTransactionRuleDto rule,
            LocalDateTime localNow,
            LocalDateTime lastProcessedAt
    ) {
        LocalDateTime anchor = LocalDateTime.of(rule.getStartDate(), rule.getExecutionTime());
        int interval = rule.getIntervalValue();
        boolean hourly = "HOURLY".equals(rule.getFrequency());
        long elapsed = lastProcessedAt == null || lastProcessedAt.isBefore(anchor)
                ? -1
                : (hourly ? ChronoUnit.HOURS : ChronoUnit.MINUTES).between(anchor, lastProcessedAt);
        long nextStep = elapsed < 0 ? 0 : elapsed / interval + 1;
        LocalDateTime occurrenceAt = hourly
                ? anchor.plusHours(nextStep * interval)
                : anchor.plusMinutes(nextStep * interval);

        while (!occurrenceAt.isAfter(localNow)
                && (rule.getEndDate() == null || !occurrenceAt.toLocalDate().isAfter(rule.getEndDate()))) {
            if (client.execute(rule.getId(), occurrenceAt.toLocalDate(), occurrenceAt.toLocalTime())) {
                log.info("Processed recurring transaction occurrence: occurrence={}", occurrenceAt);
            } else {
                log.info("Recurring transaction occurrence already exists: occurrence={}", occurrenceAt);
            }
            occurrenceAt = hourly ? occurrenceAt.plusHours(interval) : occurrenceAt.plusMinutes(interval);
        }
    }

    private LocalDateTime latestProcessedAt(SchedulerRecurringTransactionRuleDto rule) {
        LocalDateTime lastExecutedAt = rule.getLastExecutedAt();
        LocalDateTime resumeAfterAt = rule.getResumeAfterAt();
        if (lastExecutedAt == null) return resumeAfterAt;
        if (resumeAfterAt == null || lastExecutedAt.isAfter(resumeAfterAt)) return lastExecutedAt;
        return resumeAfterAt;
    }

    private boolean isOccurrence(SchedulerRecurringTransactionRuleDto rule, LocalDate date) {
        if (date.isBefore(rule.getStartDate()) || (rule.getEndDate() != null && date.isAfter(rule.getEndDate()))) {
            return false;
        }
        int interval = rule.getIntervalValue();
        return switch (rule.getFrequency()) {
            case "DAILY" -> ChronoUnit.DAYS.between(rule.getStartDate(), date) % interval == 0;
            case "WEEKLY" -> date.getDayOfWeek().getValue() == rule.getDayOfWeek()
                    && ChronoUnit.WEEKS.between(
                    rule.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ) % interval == 0;
            case "MONTHLY" -> monthlyOccurrence(rule, date, interval);
            case "YEARLY" -> yearlyOccurrence(rule, date, interval);
            default -> throw new IllegalStateException("Unsupported recurrence frequency " + rule.getFrequency());
        };
    }

    private boolean monthlyOccurrence(SchedulerRecurringTransactionRuleDto rule, LocalDate date, int interval) {
        long monthOffset = ChronoUnit.MONTHS.between(
                YearMonth.from(rule.getStartDate()), YearMonth.from(date)
        );
        if (monthOffset < 0 || monthOffset % interval != 0) return false;
        return switch (rule.getMonthlyPattern()) {
            case "DAY_OF_MONTH" -> date.getDayOfMonth() == rule.getDayOfMonth();
            case "LAST_DAY" -> date.equals(YearMonth.from(date).atEndOfMonth());
            case "LAST_WORKING_DAY" -> date.equals(lastWorkingDayOfMonth(rule, YearMonth.from(date)));
            default -> throw new IllegalStateException("Unsupported monthly pattern " + rule.getMonthlyPattern());
        };
    }

    private boolean yearlyOccurrence(SchedulerRecurringTransactionRuleDto rule, LocalDate date, int interval) {
        int yearOffset = date.getYear() - rule.getStartDate().getYear();
        return yearOffset >= 0
                && yearOffset % interval == 0
                && date.getMonthValue() == rule.getMonthOfYear()
                && date.getDayOfMonth() == rule.getDayOfMonth();
    }

    private LocalDate lastWorkingDayOfMonth(SchedulerRecurringTransactionRuleDto rule, YearMonth month) {
        Set<LocalDate> holidays = holidayClient.fetchPublicHolidays(
                rule.getCountryCode(), rule.getCountryName(),
                rule.getStateCode(), rule.getStateName(), month.getYear()
        );
        LocalDate date = month.atEndOfMonth();
        while (YearMonth.from(date).equals(month)
                && (date.getDayOfWeek().getValue() > 5 || holidays.contains(date))) {
            date = date.minusDays(1);
        }
        if (!YearMonth.from(date).equals(month)) {
            throw new IllegalStateException("No working days found for " + month);
        }
        return date;
    }
}
