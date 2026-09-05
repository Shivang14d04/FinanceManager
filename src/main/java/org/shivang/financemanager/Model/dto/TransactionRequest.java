package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record TransactionRequest(
        BigDecimal amount,
        String date,
        String category,
        String description
) {
}
