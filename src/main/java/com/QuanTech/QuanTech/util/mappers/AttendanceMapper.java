package com.QuanTech.QuanTech.util.mappers;

import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.entity.Attendance;

public class AttendanceMapper {
    private AttendanceMapper() {}

    public static AttendanceResponseDTO attendanceEntityToDto(Attendance a) {
        return new AttendanceResponseDTO(
                a.getAttendanceId(),
                a.getDate(),
                a.getCheckIn(),
                a.getCheckOut(),
                a.getHoursWorked(),
                a.getAttendanceStatus(),
                a.getLocation()
        );
    }
}
