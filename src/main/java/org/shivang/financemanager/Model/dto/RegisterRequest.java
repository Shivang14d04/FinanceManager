package org.shivang.financemanager.Model.dto;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String phoneNumber
) {
}
