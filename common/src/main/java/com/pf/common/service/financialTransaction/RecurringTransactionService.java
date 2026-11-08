package com.pf.common.service.financialTransaction;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.generic.SchedulerUserConfig;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.recurringTransaction.RecurringTransactionRuleDto;
import com.pf.common.dto.recurringTransaction.SchedulerRecurringTransactionRuleDto;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.financialTransaction.FinancialTransaction;
import com.pf.common.entity.financialTransaction.RecurringTransactionExecution;
import com.pf.common.entity.financialTransaction.RecurringTransactionRule;
import com.pf.common.entity.financialTransaction.RecurringTransactionRuleView;
import com.pf.common.entity.settings.UserReferenceValue;
import com.pf.common.entity.userManagement.User;
import com.pf.common.repository.categoryManagement.UserSubcategoryRepository;
import com.pf.common.repository.financialTransaction.FinancialTransactionRepository;
import com.pf.common.repository.financialTransaction.RecurringTransactionExecutionRepository;
import com.pf.common.repository.financialTransaction.RecurringTransactionRuleRepository;
import com.pf.common.repository.setting.ReferenceValueRepository;
import com.pf.common.repository.user.UserPermissionViewRepository;
import com.pf.common.service.generic.BaseService;
import com.pf.common.service.generic.CommonService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionService extends BaseService {
    private final RecurringTransactionRuleRepository ruleRepository;
    private final RecurringTransactionExecutionRepository executionRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final UserSubcategoryRepository subcategoryRepository;
    private final ReferenceValueRepository referenceValueRepository;
    private final UserPermissionViewRepository userPermissionViewRepository;
    private final CommonService commonService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<RecurringTransactionRuleDto> fetchRules() {
        return ruleRepository.findByAdminUser_IdOrderByIdDesc(fetchAdminUserId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public GridResult fetchGridData(SearchCriteria criteria) {
        addAdminUserGridFilter(criteria, "adminUserId");
        long totalRecords = criteria.isLoadAllData()
                ? 0
                : getCountBySearchCriteria(RecurringTransactionRuleView.class, criteria);
        if (!criteria.isLoadAllData() && totalRecords == 0) {
            return gridResult(0, List.of());
        }
        List<RecurringTransactionRuleView> rows =
                getDataBySearchCriteria(RecurringTransactionRuleView.class, criteria);
        return gridResult(totalRecords, rows);
    }

    @Transactional
    public ApiResponse deleteRules(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return failure("No recurring transaction rules were selected");
        }
        List<RecurringTransactionRule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.stream().distinct().count()) {
            return failure("One or more recurring transaction rules were not found");
        }
        ruleRepository.deleteAll(rules);
        return success("Recurring transaction rules deleted successfully");
    }

    @Transactional
    public ApiResponse setRulesActive(List<Long> ruleIds, boolean active) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return failure("No recurring transaction rules were selected");
        }
        Long adminUserId = fetchAdminUserId();
        List<RecurringTransactionRule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.stream().distinct().count()) {
            return failure("One or more recurring transaction rules were not found");
        }
        User user = fetchLoginUser();
        boolean resumingRules = active && rules.stream().anyMatch(rule -> !Boolean.TRUE.equals(rule.getActive()));
        LocalDateTime resumeAfterAt = resumingRules ? schedulerDateTimeForAdmin(adminUserId) : null;
        rules.forEach(rule -> {
            if (resumingRules && !Boolean.TRUE.equals(rule.getActive())) {
                rule.setResumeAfterAt(resumeAfterAt);
            }
            rule.setActive(active);
            rule.setUpdatedBy(user);
        });
        ruleRepository.saveAll(rules);
        return success(active
                ? "Recurring transaction rules activated successfully"
                : "Recurring transaction rules inactivated successfully");
    }

    @Transactional
    public ApiResponse saveRule(RecurringTransactionRuleDto dto) {
        if (!isValid(dto)) return failure("Enter a rule name and complete all required recurrence fields");
        Long adminId = fetchAdminUserId();
        UserSubcategory subcategory = subcategoryRepository.findById(dto.getSubcategoryId()).orElse(null);
        if (subcategory == null || !Boolean.TRUE.equals(subcategory.getActive())
                || !Boolean.TRUE.equals(subcategory.getUserCategory().getActive())
                || !adminId.equals(subcategory.getUserCategory().getUser().getId())) {
            return failure("Selected subcategory was not found");
        }

        if (dto.getCategoryId() != null
                && !dto.getCategoryId().equals(subcategory.getUserCategory().getId())) {
            return failure("Selected subcategory does not belong to the selected category");
        }
        UserReferenceValue location = null;
        if (dto.getLocationId() != null) {
            location = referenceValueRepository.findById(dto.getLocationId()).orElse(null);
            if (location == null || !adminId.equals(location.getUser().getId()) || !location.isActive()) {
                return failure("Selected location was not found");
            }
        }

        User user = fetchLoginUser();
        RecurringTransactionRule rule;
        boolean activating;
        if (dto.getId() == null) {
            rule = new RecurringTransactionRule();
            rule.setAdminUser(entityManager.getReference(User.class, adminId));
            rule.setCreatedBy(user);
            rule.setActive(true);
            activating = false;
        } else {
            rule = ruleRepository.findById(dto.getId()).orElse(null);
            if (rule == null) {
                return failure("Recurring transaction rule was not found");
            }
            rule.setUpdatedBy(user);
            activating = !Boolean.TRUE.equals(rule.getActive())
                    && (dto.getActive() == null || dto.getActive());
            rule.setActive(dto.getActive() == null || dto.getActive());
        }
        if (activating) rule.setResumeAfterAt(schedulerDateTimeForAdmin(adminId));
        rule.setRuleName(dto.getRuleName().trim());
        rule.setSubcategory(subcategory);
        rule.setLocation(location);
        rule.setAmount(dto.getAmount());
        rule.setRemarks(StringUtils.hasText(dto.getRemarks()) ? dto.getRemarks().trim() : null);
        rule.setFrequency(dto.getFrequency());
        rule.setIntervalValue(dto.getIntervalValue());
        rule.setDayOfWeek(dto.getDayOfWeek());
        rule.setDayOfMonth(dto.getDayOfMonth());
        rule.setMonthOfYear(dto.getMonthOfYear());
        rule.setMonthlyPattern(dto.getMonthlyPattern());
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setExecutionTime(dto.getExecutionTime());
        ruleRepository.save(rule);
        return success("Recurring transaction rule saved successfully");
    }

    public List<SchedulerRecurringTransactionRuleDto> fetchSchedulerRules() {
        List<SchedulerUserConfig> configurations = commonService.fetchSchedulerUserConfigs();
        Map<Long, SchedulerUserConfig> configByUser = configurations.stream()
                .collect(Collectors.toMap(SchedulerUserConfig::userId, Function.identity()));
        return ruleRepository.findRunnableRules().stream().map(rule -> {
            SchedulerUserConfig config = configByUser.get(rule.getAdminUser().getId());
            if (config == null || !StringUtils.hasText(config.timeZone())) {
                throw new IllegalStateException("Missing scheduler timezone for admin user " + rule.getAdminUser().getId());
            }
            return SchedulerRecurringTransactionRuleDto.builder()
                    .id(rule.getId())
                    .adminUserId(rule.getAdminUser().getId())
                    .subcategoryId(rule.getSubcategory().getId())
                    .locationId(rule.getLocation() == null ? null : rule.getLocation().getId())
                    .amount(rule.getAmount())
                    .remarks(rule.getRemarks())
                    .frequency(rule.getFrequency())
                    .intervalValue(rule.getIntervalValue())
                    .dayOfWeek(rule.getDayOfWeek())
                    .dayOfMonth(rule.getDayOfMonth())
                    .monthOfYear(rule.getMonthOfYear())
                    .monthlyPattern(rule.getMonthlyPattern())
                    .startDate(rule.getStartDate())
                    .endDate(rule.getEndDate())
                    .executionTime(rule.getExecutionTime())
                    .timeZone(config.timeZone())
                    .countryCode(config.countryCode())
                    .countryName(config.countryName())
                    .stateCode(config.stateCode())
                    .stateName(config.stateName())
                    .lastExecutedAt(latestExecutedAt(rule))
                    .resumeAfterAt(rule.getResumeAfterAt())
                    .build();
        }).toList();
    }

    @Transactional
    public boolean execute(Long ruleId, LocalDate occurrenceDate, LocalTime occurrenceTime) {
        LocalDateTime occurrenceAt = LocalDateTime.of(occurrenceDate, occurrenceTime);
        RecurringTransactionRule rule = ruleRepository.findByIdForUpdate(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction rule not found: " + ruleId));
        if (!Boolean.TRUE.equals(rule.getActive())
                || !Boolean.TRUE.equals(rule.getSubcategory().getActive())
                || !Boolean.TRUE.equals(rule.getSubcategory().getUserCategory().getActive())
                || occurrenceAt.isBefore(LocalDateTime.of(rule.getStartDate(), rule.getExecutionTime()))
                || (rule.getResumeAfterAt() != null && !occurrenceAt.isAfter(rule.getResumeAfterAt()))
                || (rule.getEndDate() != null && occurrenceDate.isAfter(rule.getEndDate()))
                || !isOccurrence(rule, occurrenceAt)) {
            return false;
        }
        if (executionRepository.existsByRule_IdAndOccurrenceDateAndOccurrenceTime(
                ruleId, occurrenceDate, occurrenceTime
        )) return false;

        User admin = rule.getAdminUser();
        User creator = rule.getCreatedBy();
        if (!canCreatorAddTransaction(rule, creator)) {
            log.warn(
                    "Skipping recurring transaction: creator {} lacks Add permission for rule {}",
                    creator.getId(), ruleId
            );
            executionRepository.save(RecurringTransactionExecution.builder()
                    .rule(rule)
                    .occurrenceDate(occurrenceDate)
                    .occurrenceTime(occurrenceTime)
                    .build());
            return true;
        }

        FinancialTransaction transaction = FinancialTransaction.builder()
                .amount(rule.getAmount())
                .transactionAt(occurrenceDate)
                .subcategory(rule.getSubcategory())
                .location(rule.getLocation())
                .remarks(rule.getRemarks())
                .adminUser(admin)
                .createdBy(creator)
                .build();
        transactionRepository.saveAndFlush(transaction);
        executionRepository.save(RecurringTransactionExecution.builder()
                .rule(rule)
                .occurrenceDate(occurrenceDate)
                .occurrenceTime(occurrenceTime)
                .financialTransaction(transaction)
                .build());
        return true;
    }

    private boolean canCreatorAddTransaction(RecurringTransactionRule rule, User creator) {
        Long creatorId = creator.getId();
        if (rule.getAdminUser().getId().equals(creatorId)) return true;

        String screenName = switch (rule.getSubcategory().getUserCategory()
                .getUserReferenceValue().getReferenceCode().trim().toUpperCase(java.util.Locale.ROOT)) {
            case "EXPENSE" -> "Expense";
            case "INCOME" -> "Income";
            case "INVESTMENT" -> "Investment";
            case "TRANSFER" -> "Transfer";
            default -> null;
        };
        if (screenName == null) {
            log.warn("Skipping recurring transaction: unsupported transaction type for rule {}", rule.getId());
            return false;
        }
        return userPermissionViewRepository
                .existsByUserIdAndModuleNameAndScreenNameAndActionNameAndHasPermissionTrue(
                        creatorId, "Financial Transaction", screenName, "Add"
                );
    }

    private boolean isValid(RecurringTransactionRuleDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getRuleName())
                || dto.getRuleName().trim().length() > 150
                || dto.getSubcategoryId() == null || dto.getAmount() == null
                || dto.getAmount().signum() == 0 || dto.getStartDate() == null
                || dto.getExecutionTime() == null || dto.getIntervalValue() == null
                || dto.getIntervalValue() < 1 || !StringUtils.hasText(dto.getFrequency())) return false;
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) return false;
        return switch (dto.getFrequency()) {
            case "DAILY", "HOURLY", "MINUTELY" -> true;
            case "WEEKLY" -> dto.getDayOfWeek() != null && dto.getDayOfWeek() >= 1 && dto.getDayOfWeek() <= 7;
            case "MONTHLY" -> "LAST_DAY".equals(dto.getMonthlyPattern())
                    || "LAST_WORKING_DAY".equals(dto.getMonthlyPattern())
                    || ("DAY_OF_MONTH".equals(dto.getMonthlyPattern())
                    && dto.getDayOfMonth() != null && dto.getDayOfMonth() >= 1 && dto.getDayOfMonth() <= 31);
            case "YEARLY" -> dto.getMonthOfYear() != null && dto.getMonthOfYear() >= 1 && dto.getMonthOfYear() <= 12
                    && dto.getDayOfMonth() != null && dto.getDayOfMonth() >= 1 && dto.getDayOfMonth() <= 31;
            default -> false;
        };
    }

    private LocalDateTime latestExecutedAt(RecurringTransactionRule rule) {
        return executionRepository
                .findFirstByRule_IdOrderByOccurrenceDateDescOccurrenceTimeDesc(rule.getId())
                .map(execution -> LocalDateTime.of(execution.getOccurrenceDate(), execution.getOccurrenceTime()))
                .orElse(null);
    }

    private LocalDateTime schedulerDateTimeForAdmin(Long adminId) {
        return commonService.fetchSchedulerUserConfigs().stream()
                .filter(configuration -> adminId.equals(configuration.userId()))
                .findFirst()
                .map(configuration -> LocalDateTime.now(ZoneId.of(configuration.timeZone())))
                .orElseThrow(() -> new IllegalStateException(
                        "Missing scheduler timezone for admin user " + adminId
                ));
    }

    private boolean isOccurrence(RecurringTransactionRule rule, LocalDateTime occurrenceAt) {
        LocalDate date = occurrenceAt.toLocalDate();
        if (date.isBefore(rule.getStartDate())
                || (rule.getEndDate() != null && date.isAfter(rule.getEndDate()))) return false;
        if ("HOURLY".equals(rule.getFrequency()) || "MINUTELY".equals(rule.getFrequency())) {
            LocalDateTime startAt = LocalDateTime.of(rule.getStartDate(), rule.getExecutionTime());
            boolean hourly = "HOURLY".equals(rule.getFrequency());
            long intervalMinutes = hourly ? rule.getIntervalValue() * 60L : rule.getIntervalValue();
            long elapsedMinutes = ChronoUnit.MINUTES.between(startAt, occurrenceAt);
            return !occurrenceAt.isBefore(startAt)
                    && occurrenceAt.getSecond() == startAt.getSecond()
                    && occurrenceAt.getNano() == startAt.getNano()
                    && elapsedMinutes % intervalMinutes == 0;
        }
        return occurrenceAt.toLocalTime().equals(rule.getExecutionTime())
                && isCalendarOccurrence(rule, date);
    }

    private boolean isCalendarOccurrence(RecurringTransactionRule rule, LocalDate date) {
        int interval = rule.getIntervalValue();
        return switch (rule.getFrequency()) {
            case "DAILY" -> ChronoUnit.DAYS.between(rule.getStartDate(), date) % interval == 0;
            case "WEEKLY" -> date.getDayOfWeek().getValue() == rule.getDayOfWeek()
                    && ChronoUnit.WEEKS.between(
                    rule.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ) % interval == 0;
            case "MONTHLY" -> {
                long monthOffset = ChronoUnit.MONTHS.between(
                        YearMonth.from(rule.getStartDate()), YearMonth.from(date)
                );
                if (monthOffset < 0 || monthOffset % interval != 0) yield false;
                yield switch (rule.getMonthlyPattern()) {
                    case "DAY_OF_MONTH" -> date.getDayOfMonth() == rule.getDayOfMonth();
                    case "LAST_DAY" -> date.equals(YearMonth.from(date).atEndOfMonth());
                    case "LAST_WORKING_DAY" -> true;
                    default -> false;
                };
            }
            case "YEARLY" -> {
                int yearOffset = date.getYear() - rule.getStartDate().getYear();
                yield yearOffset >= 0
                        && yearOffset % interval == 0
                        && date.getMonthValue() == rule.getMonthOfYear()
                        && date.getDayOfMonth() == rule.getDayOfMonth();
            }
            default -> false;
        };
    }

    private RecurringTransactionRuleDto toDto(RecurringTransactionRule rule) {
        return RecurringTransactionRuleDto.builder()
                .id(rule.getId())
                .createdById(rule.getCreatedBy().getId())
                .createdBy(rule.getCreatedBy().getUsername())
                .createdAt(rule.getCreatedAt())
                .updatedBy(rule.getUpdatedBy() == null ? null : rule.getUpdatedBy().getUsername())
                .updatedAt(rule.getUpdatedAt())
                .ruleName(rule.getRuleName())
                .subcategoryId(rule.getSubcategory().getId())
                .categoryId(rule.getSubcategory().getUserCategory().getId())
                .transactionType(rule.getSubcategory().getUserCategory().getUserReferenceValue().getReferenceCode())
                .categoryName(rule.getSubcategory().getUserCategory().getCategoryName())
                .subcategoryName(rule.getSubcategory().getSubcategoryName())
                .locationId(rule.getLocation() == null ? null : rule.getLocation().getId())
                .amount(rule.getAmount())
                .remarks(rule.getRemarks())
                .frequency(rule.getFrequency())
                .intervalValue(rule.getIntervalValue())
                .dayOfWeek(rule.getDayOfWeek())
                .dayOfMonth(rule.getDayOfMonth())
                .monthOfYear(rule.getMonthOfYear())
                .monthlyPattern(rule.getMonthlyPattern())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .executionTime(rule.getExecutionTime())
                .active(rule.getActive())
                .build();
    }
}
