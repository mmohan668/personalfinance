package com.pf.common.dto.recurringTransaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerRecurringTransactionRuleDto {
    private Long id;
    private Long adminUserId;
    private Long subcategoryId;
    private Long locationId;
    private BigDecimal amount;
    private String remarks;
    private String frequency;
    private Integer intervalValue;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private String monthlyPattern;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime executionTime;
    private String timeZone;
    private String countryCode;
    private String countryName;
    private String stateCode;
    private String stateName;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime resumeAfterAt;
}
