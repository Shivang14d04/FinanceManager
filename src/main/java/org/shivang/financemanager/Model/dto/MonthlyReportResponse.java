package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlyReportResponse(
        int month,
        int year,
        Map<String, BigDecimal> totalIncome,
        Map<String, BigDecimal> totalExpenses,
        BigDecimal netSavings
) {
}
