package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record GoalResponse(
        Long id,
        String goalName,
        BigDecimal targetAmount,
        String targetDate,
        String startDate,
        BigDecimal currentProgress,
        BigDecimal progressPercentage,
        BigDecimal remainingAmount
) {
}
