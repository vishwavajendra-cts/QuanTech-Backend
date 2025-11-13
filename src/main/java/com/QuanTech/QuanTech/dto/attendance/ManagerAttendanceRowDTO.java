package com.QuanTech.QuanTech.dto.attendance;

import com.QuanTech.QuanTech.constants.enums.AttendanceStatus;

import java.time.OffsetDateTime;

public record ManagerAttendanceRowDTO(
    String displayEmployeeId,
    String employeeName,
    OffsetDateTime checkIn,
    OffsetDateTime checkOut,
    double hoursWorked,
    AttendanceStatus attendanceStatus
) {
}
