package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceDTO;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceResponseDTO;

import java.util.List;

public interface LeaveBalanceService {
    List<LeaveBalanceResponseDTO> getLeaveBalancesByEmployeeId(String employeeId);

    LeaveBalanceDTO createLeaveBalance(String employeeId, LeaveType leaveType, int leaveBalance);
}
