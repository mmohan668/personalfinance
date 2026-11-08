package com.pf.scheduler.monthly;

import com.pf.common.dto.generic.SchedulerUserConfig;
import com.pf.scheduler.holiday.PublicHolidayClient;
import com.pf.scheduler.recurring.AsyncRecurringTransactionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Component
public class MonthlyScheduler implements SchedulingConfigurer {

    private final AsyncRecurringTransactionClient asyncClient;
    private final PublicHolidayClient publicHolidayClient;

    public MonthlyScheduler(
            AsyncRecurringTransactionClient asyncClient,
            PublicHolidayClient publicHolidayClient
    ) {
        this.asyncClient = asyncClient;
        this.publicHolidayClient = publicHolidayClient;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        for (SchedulerUserConfig configuration : asyncClient.fetchConfigurations()) {
            ZoneId zoneId = ZoneId.of(configuration.timeZone());
            for (ScheduleType scheduleType : ScheduleType.values()) {
                taskRegistrar.addTriggerTask(
                        () -> trigger(configuration.userId(), scheduleType),
                        context -> nextExecution(context, configuration, scheduleType, zoneId)
                );
            }
            log.info("Registered monthly schedules for admin user {}", configuration.userId());
        }
    }

    private void trigger(Long userId, ScheduleType scheduleType) {
        log.info("{} scheduler triggered for admin user {}", scheduleType, userId);
    }

    private Instant nextExecution(
            TriggerContext triggerContext,
            SchedulerUserConfig configuration,
            ScheduleType scheduleType,
            ZoneId zoneId
    ) {
        Instant reference = triggerContext.lastCompletion();
        if (reference == null) {
            reference = Instant.now();
        }

        try {
            YearMonth month = YearMonth.from(reference.atZone(zoneId));
            while (true) {
                LocalDate candidate = scheduledDate(month, configuration, scheduleType);
                Instant execution = LocalDateTime.of(candidate, LocalTime.of(6, 0))
                        .atZone(zoneId)
                        .toInstant();
                if (execution.isAfter(reference)) {
                    return execution;
                }
                month = month.plusMonths(1);
            }
        } catch (RuntimeException exception) {
            log.error(
                    "Unable to calculate {} schedule for admin user {}",
                    scheduleType,
                    configuration.userId(),
                    exception
            );
            throw exception;
        }
    }

    private LocalDate scheduledDate(
            YearMonth month,
            SchedulerUserConfig configuration,
            ScheduleType scheduleType
    ) {
        return switch (scheduleType) {
            case FIRST_DAY -> month.atDay(1);
            case LAST_DAY -> month.atEndOfMonth();
            case LAST_WORKING_DAY -> lastWorkingDayOfMonth(
                    month,
                    publicHolidayClient.fetchPublicHolidays(
                            configuration.countryCode(),
                            configuration.countryName(),
                            configuration.stateCode(),
                            configuration.stateName(),
                            month.getYear()
                    )
            );
        };
    }

    static LocalDate lastWorkingDayOfMonth(YearMonth month, Set<LocalDate> publicHolidays) {
        LocalDate date = month.atEndOfMonth();
        while (YearMonth.from(date).equals(month) && !isWorkingDay(date, publicHolidays)) {
            date = date.minusDays(1);
        }
        if (!YearMonth.from(date).equals(month)) {
            throw new IllegalStateException("No working days found for " + month);
        }
        return date;
    }

    private static boolean isWorkingDay(LocalDate date, Set<LocalDate> publicHolidays) {
        return date.getDayOfWeek().getValue() <= 5 && !publicHolidays.contains(date);
    }

    private enum ScheduleType {
        FIRST_DAY,
        LAST_DAY,
        LAST_WORKING_DAY
    }
}
