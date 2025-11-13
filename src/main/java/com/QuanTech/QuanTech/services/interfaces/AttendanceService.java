package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.CheckInRequestDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceDisplayByDateResponseDTO;

import java.util.List;

public interface AttendanceService {
    AttendanceResponseDTO getLatestAttendance(String employeeId);

    List<AttendanceResponseDTO> getAttendanceHistory(String employeeId);

    AttendanceResponseDTO checkIn(String employeeId, CheckInRequestDTO checkInRequestDTO);

    AttendanceResponseDTO checkOut(String employeeId);

    ManagerAttendanceDisplayByDateResponseDTO getTeamsAttendanceByDate(String managerId, String date);
}
