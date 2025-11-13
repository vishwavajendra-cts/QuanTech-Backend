package com.QuanTech.QuanTech.dto.leaveRequests;

import com.QuanTech.QuanTech.constants.enums.LeaveType;

import java.time.LocalDate;

public record ManagerLeaveRequestDashboardResponseDTO(
        String leaveRequestId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate
) {
}
