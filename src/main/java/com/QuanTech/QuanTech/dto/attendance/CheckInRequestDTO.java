package com.QuanTech.QuanTech.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequestDTO(
    @NotBlank(message = "Location is required")
    String location
) {
}
