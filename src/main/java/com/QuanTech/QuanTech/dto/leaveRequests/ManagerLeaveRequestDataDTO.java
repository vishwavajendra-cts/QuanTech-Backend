package com.QuanTech.QuanTech.dto.leaveRequests;

public record ManagerLeaveRequestDataDTO(
        long pending,
        long approved,
        long rejected,
        long onLeaveToday
) {
}
