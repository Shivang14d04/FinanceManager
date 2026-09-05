package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record GoalUpdateRequest(
        BigDecimal targetAmount,
        String targetDate
) {
}
