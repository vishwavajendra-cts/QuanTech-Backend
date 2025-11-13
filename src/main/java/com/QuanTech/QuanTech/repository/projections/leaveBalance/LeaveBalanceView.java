package com.QuanTech.QuanTech.repository.projections.leaveBalance;

import com.QuanTech.QuanTech.constants.enums.LeaveType;

public interface LeaveBalanceView {
    String getBalanceId();

    LeaveType getLeaveType();

    int getLeaveBalance();
}
