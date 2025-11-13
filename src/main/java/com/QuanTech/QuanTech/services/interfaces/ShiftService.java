package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.shift.CreateShiftDateRequestDTO;
import com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamShiftTableRowDTO;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {
    ShiftResponseDTO createShift(CreateShiftDateRequestDTO request, String managerId);

    List<ShiftResponseDTO> getEmployeeShifts(String employeeId);

    List<ShiftResponseDTO> getTeamsShiftByManager(String managerId);

    List<TeamShiftTableRowDTO> getTeamShiftsByManagerAndDatePicker(String managerId, LocalDate date);
}
