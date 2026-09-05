package org.shivang.financemanager.Model.dto;

import java.math.BigDecimal;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String date,
        String category,
        String description,
        String type
) {
}
