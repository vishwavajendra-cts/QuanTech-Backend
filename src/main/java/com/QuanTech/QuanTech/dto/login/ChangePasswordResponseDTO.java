package com.QuanTech.QuanTech.dto.login;

import jakarta.validation.constraints.Email;
import org.springframework.data.repository.cdi.Eager;

public record ChangePasswordResponseDTO(
        @Email String email,
        String message
) {
}
