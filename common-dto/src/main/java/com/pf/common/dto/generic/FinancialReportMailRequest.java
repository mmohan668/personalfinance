package com.pf.common.dto.generic;

public record FinancialReportMailRequest(
        String emailId,
        String displayName,
        String periodLabel,
        String csvFileName,
        String csvContent
) {
}
