package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.shiftSwapRequest.CreateShiftSwapRequestDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;

import java.util.List;

public interface ShiftSwapRequestService {
    ShiftSwapResponseDTO createSwapRequest(CreateShiftSwapRequestDTO createSwapDto);

    List<ShiftSwapQueryResponseDTO> getSwapRequestsForEmployee(String employeeId);

    List<ShiftSwapQueryResponseDTO> getTeamSwapRequests(String managerId);

    ShiftSwapResponseDTO approveSwapRequest(String managerId, String swapRequestId);

    ShiftSwapResponseDTO rejectSwapRequest(String managerId, String swapRequestId);
}
