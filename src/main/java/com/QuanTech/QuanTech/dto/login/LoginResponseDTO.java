package com.QuanTech.QuanTech.dto.login;

import com.QuanTech.QuanTech.constants.enums.Role;
import jakarta.validation.constraints.Email;

import java.util.UUID;

public record LoginResponseDTO(
        UUID uuid,
        @Email String email,
        Role role,
        String message,
        String employeeId,
        String token
) {
}
