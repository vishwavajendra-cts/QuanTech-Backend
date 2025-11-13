package com.QuanTech.QuanTech.util.mappers;

import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceDTO;
import com.QuanTech.QuanTech.entity.LeaveBalance;

public class LeaveBalanceMapper {
    private LeaveBalanceMapper() {
    }

    public static LeaveBalanceDTO leaveBalanceEntityToDTO(LeaveBalance leaveBalance) {
        if (leaveBalance == null) {
            return null;
        }

        return new LeaveBalanceDTO(
                leaveBalance.getId(),
                leaveBalance.getBalanceId(),
                leaveBalance.getLeaveType(),
                leaveBalance.getLeaveBalance()
        );
    }
}
