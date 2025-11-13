package com.QuanTech.QuanTech.dto.leaveRequests;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@NotNull
public record LeaveRequestResponseDTO(
        String leaveRequestId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long days,
        LeaveStatus status,
        OffsetDateTime requestDate,
        String reason
) {
}
