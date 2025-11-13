package com.QuanTech.QuanTech.dto.leaveBalance;

import com.QuanTech.QuanTech.constants.enums.LeaveType;

public record LeaveBalanceResponseDTO(
        String balanceId,
        LeaveType leaveType,
        int leaveBalance
) {
}
