package com.QuanTech.QuanTech.dto.leaveRequests;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;

import java.time.LocalDate;

public record EmployeeLeaveRequestDashboardResponseDTO(
        String leaveRequestId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status
) {
}
