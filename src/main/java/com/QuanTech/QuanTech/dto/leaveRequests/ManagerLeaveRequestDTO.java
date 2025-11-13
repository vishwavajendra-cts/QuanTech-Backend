package com.QuanTech.QuanTech.dto.leaveRequests;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;

import java.time.LocalDate;
import java.util.UUID;

public record ManagerLeaveRequestDTO(
        UUID requestId,
        UUID employeeId,
        String displayEmployeeId,
        String employeeFirstName,
        String employeeLastName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long days,
        LeaveStatus status,
        String reason
) {
}
