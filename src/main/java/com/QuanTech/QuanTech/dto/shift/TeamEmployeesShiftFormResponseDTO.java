package com.QuanTech.QuanTech.dto.shift;

import java.util.UUID;

public record TeamEmployeesShiftFormResponseDTO(
        UUID id,
        String firstName,
        String lastName
) {
}
