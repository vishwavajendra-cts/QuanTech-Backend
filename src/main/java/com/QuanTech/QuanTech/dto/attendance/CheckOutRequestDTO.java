package com.QuanTech.QuanTech.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record CheckOutRequestDTO(
        @NotBlank
        String employeeId
) {
}
