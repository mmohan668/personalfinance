package com.pf.common.entity.financialTransaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_report_delivery", uniqueConstraints = @UniqueConstraint(
        name = "uq_financial_report_delivery",
        columnNames = {"admin_user_id", "frequency", "period_start", "period_end"}
))
@Getter
@Setter
@NoArgsConstructor
public class FinancialReportDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(nullable = false, length = 16)
    private String frequency;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Column(name = "queued_at")
    private LocalDateTime queuedAt;

    @Column(name = "error_message")
    private String errorMessage;
}
