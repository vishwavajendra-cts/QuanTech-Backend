package com.QuanTech.QuanTech.dto.login;

public record CreateLoginCredentialResponseDTO(
        String loginCredentialId,

        String message,

        String employeeId
) {
}
