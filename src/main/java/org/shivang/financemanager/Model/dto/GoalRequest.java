package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record GoalRequest(
        String goalName,
        BigDecimal targetAmount,
        String targetDate,
        String startDate
) {
}
