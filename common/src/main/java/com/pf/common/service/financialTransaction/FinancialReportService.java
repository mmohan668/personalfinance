package com.pf.common.service.financialTransaction;

import com.pf.common.constants.CommonConstants;
import com.pf.common.dto.generic.FinancialReportMailRequest;
import com.pf.common.entity.financialTransaction.FinancialReportDelivery;
import com.pf.common.entity.financialTransaction.FinancialTransactionView;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.entity.userManagement.User;
import com.pf.common.repository.financialTransaction.FinancialReportDeliveryRepository;
import com.pf.common.repository.financialTransaction.FinancialTransactionRepository;
import com.pf.common.repository.setting.SystemConfigRepository;
import com.pf.common.repository.user.UserRepository;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportService {
    private static final Pattern DAY_INTERVAL_PATTERN = Pattern.compile("^(?:EVERY\\s*)?(\\d+)\\s*DAYS?$", Pattern.CASE_INSENSITIVE);
    private final UserRepository userRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinancialReportDeliveryRepository deliveryRepository;
    private final ActiveMqProducer producer;

    @Transactional
    public void dispatchReports(LocalDate today) {
        List<Long> userIds = userRepository.findAllAdminUserIds();
        if (userIds.isEmpty()) return;
        Map<Long, Map<String, SystemConfig>> configs = new HashMap<>();
        for (SystemConfig config : systemConfigRepository.findFinancialReportSettingsByUserIdIn(userIds)) {
            configs.computeIfAbsent(config.getUserId(), ignored -> new HashMap<>())
                    .put(config.getConfigName().trim().toLowerCase(Locale.ROOT), config);
        }

        for (Long userId : userIds) {
            Map<String, SystemConfig> userConfig = configs.get(userId);
            if (userConfig == null) continue;
            SystemConfig enableConfig = userConfig.get("enable reports");
            SystemConfig frequencyConfig = userConfig.get("report frequency");
            if (enableConfig == null || frequencyConfig == null
                    || !"yes".equalsIgnoreCase(enableConfig.getConfigValue().getReferenceCode())) continue;
            String frequency = frequencyConfig.getConfigValue().getReferenceCode();
            LocalDate frequencyAnchor = frequencyConfig.getUpdatedAt() == null
                    ? frequencyConfig.getCreatedAt().toLocalDate()
                    : frequencyConfig.getUpdatedAt().toLocalDate();
            LocalDate[] period = periodFor(frequency, today, frequencyAnchor);
            if (period == null) continue;
            dispatchOne(userId, frequency.toUpperCase(Locale.ROOT), period[0], period[1]);
        }
    }

    private LocalDate[] periodFor(String frequency, LocalDate currentQuarterStart, LocalDate frequencyAnchor) {
        Matcher dayInterval = DAY_INTERVAL_PATTERN.matcher(frequency.trim());
        if (dayInterval.matches()) {
            int intervalDays;
            try {
                intervalDays = Integer.parseInt(dayInterval.group(1));
            } catch (NumberFormatException exception) {
                return null;
            }
            if (intervalDays < 1 || frequencyAnchor == null) return null;
            long daysSinceConfigured = ChronoUnit.DAYS.between(frequencyAnchor, currentQuarterStart);
            if (daysSinceConfigured <= 0 || daysSinceConfigured % intervalDays != 0) return null;
            return new LocalDate[]{currentQuarterStart.minusDays(intervalDays), currentQuarterStart};
        }
        String normalizedFrequency = frequency.toUpperCase(Locale.ROOT).replaceAll("[\\s_-]", "");
        return switch (normalizedFrequency) {
            case "DAILY" -> new LocalDate[]{currentQuarterStart.minusDays(1), currentQuarterStart};
            case "WEEKLY" -> {
                if (currentQuarterStart.getDayOfWeek() != DayOfWeek.MONDAY) yield null;
                yield new LocalDate[]{currentQuarterStart.minusDays(8), currentQuarterStart.minusDays(1)};
            }
            case "MONTHLY" -> {
                if (currentQuarterStart.getDayOfMonth() != 1) yield null;
                YearMonth previous = YearMonth.from(currentQuarterStart).minusMonths(1);
                yield new LocalDate[]{previous.atDay(1), previous.plusMonths(1).atDay(1)};
            }
            case "QUARTERLY" -> {
                int month = currentQuarterStart.getMonthValue();
                if (currentQuarterStart.getDayOfMonth() != 1 || (month - 1) % 3 != 0) yield null;
                LocalDate previousQuarterStart = currentQuarterStart.minusMonths(3);
                yield new LocalDate[]{previousQuarterStart, currentQuarterStart};
            }
            case "HALFYEARLY" -> {
                int month = currentQuarterStart.getMonthValue();
                if (currentQuarterStart.getDayOfMonth() != 1 || (month != 1 && month != 7)) yield null;
                yield new LocalDate[]{currentQuarterStart.minusMonths(6), currentQuarterStart};
            }
            case "YEARLY" -> {
                if (currentQuarterStart.getMonthValue() != 1 || currentQuarterStart.getDayOfMonth() != 1) yield null;
                LocalDate previousYear = currentQuarterStart.minusYears(1);
                yield new LocalDate[]{previousYear.withDayOfYear(1), currentQuarterStart};
            }
            default -> null;
        };
    }

    private void dispatchOne(Long userId, String frequency, LocalDate from, LocalDate to) {
        FinancialReportDelivery delivery = deliveryRepository
                .findByAdminUserIdAndFrequencyAndPeriodStartAndPeriodEnd(userId, frequency, from, to)
                .orElseGet(FinancialReportDelivery::new);
        if ("QUEUED".equals(delivery.getStatus())) return;
        delivery.setAdminUserId(userId);
        delivery.setFrequency(frequency);
        delivery.setPeriodStart(from);
        delivery.setPeriodEnd(to);
        delivery.setStatus("PROCESSING");
        delivery.setAttemptedAt(LocalDateTime.now());
        delivery.setErrorMessage(null);
        deliveryRepository.saveAndFlush(delivery);
        try {
            User user = userRepository.findById(userId).orElseThrow();
            List<FinancialTransactionView> transactions = transactionRepository
                    .fetchFinancialReportTransactions(from, to, userId);
            String csv = createCsv(transactions);
            String label = from + " to " + to.minusDays(1);
            producer.sendMessage(CommonConstants.FINANCIAL_REPORT_MAIL,
                    new FinancialReportMailRequest(user.getEmail(), user.getDisplayName(), label,
                            "financial-transactions-" + from + "-to-" + to.minusDays(1) + ".csv", csv));
            delivery.setStatus("QUEUED");
            delivery.setQueuedAt(LocalDateTime.now());
            deliveryRepository.save(delivery);
            log.info("Queued financial transaction report: adminUserId={}, frequency={}, from={}, to={}",
                    userId, frequency, from, to);
        } catch (RuntimeException exception) {
            delivery.setStatus("FAILED");
            delivery.setErrorMessage(exception.getMessage());
            deliveryRepository.save(delivery);
            log.error("Unable to queue financial transaction report: adminUserId={}, from={}, to={}",
                    userId, from, to, exception);
        }
    }

    private String createCsv(List<FinancialTransactionView> transactions) {
        StringBuilder csv = new StringBuilder("Date,Amount,Transaction Type,Category,Subcategory,Remarks\r\n");
        for (FinancialTransactionView row : transactions) {
            append(csv, row.getTransactionAt());
            append(csv, row.getAmount());
            append(csv, row.getTransactionType());
            append(csv, row.getCategoryName());
            append(csv, row.getSubcategoryName());
            append(csv, row.getRemarks());
            csv.append("\r\n");
        }
        return csv.toString();
    }

    private void append(StringBuilder csv, Object value) {
        if (csv.charAt(csv.length() - 1) != '\n') csv.append(',');
        String text = value == null ? "" : value.toString();
        csv.append('"').append(text.replace("\"", "\"\"")).append('"');
    }
}
