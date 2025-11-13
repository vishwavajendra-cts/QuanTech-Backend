package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.leaveRequests.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LeaveRequestService {
    @Transactional
    LeaveRequestResponseDTO createLeaveRequest(String employeeId, LeaveRequestCreateRequestDTO request);

    List<LeaveRequestResponseDTO> getEmployeeLeaveRequests(String employeeId);

    List<ManagerLeaveRequestDTO> getTeamLeaveRequests(String managerId);

    void actionOnLeaveRequest(String managerId, String requestId, LeaveRequestActionDTO leaveRequestActionDTO);

    ManagerLeaveRequestDataDTO getLeaveRequestsStatsByManager(String managerId);

    List<ManagerLeaveRequestDashboardResponseDTO> getLeaveRequestManagerDashboard(String managerId);

    List<EmployeeLeaveRequestDashboardResponseDTO> getLeaveRequestEmployeeDashboard(String employeeId);
}
