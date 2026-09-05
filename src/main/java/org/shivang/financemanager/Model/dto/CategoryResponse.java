package org.shivang.financemanager.Model.dto;

public record CategoryResponse(
        Long id,
        String name,
        String type,
        boolean isCustom
) {
}
