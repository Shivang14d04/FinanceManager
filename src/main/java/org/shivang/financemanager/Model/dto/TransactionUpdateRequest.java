package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record TransactionUpdateRequest(
        BigDecimal amount,
        String category,
        String description
) {
}
