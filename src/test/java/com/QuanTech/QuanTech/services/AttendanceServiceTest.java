package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.enums.AttendanceStatus;
import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.CheckInRequestDTO;
import com.QuanTech.QuanTech.entity.Attendance;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.exception.custom.ActiveAttendanceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.repository.AttendanceRepository;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.services.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkIn_happyPath_savesAttendanceAndReturnsDto() {
        UUID empId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(empId);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findLatestByEmployeeAndStatus(eq(empId), eq(AttendanceStatus.ACTIVE))).thenReturn(List.of());

        Attendance saved = new Attendance();
        saved.setAttendanceId("ATT-XYZ");
        saved.setEmployee(employee);
        saved.setDate(OffsetDateTime.now());
        saved.setCheckIn(OffsetDateTime.now());
        saved.setAttendanceStatus(AttendanceStatus.ACTIVE);
        saved.setLocation("Office");

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(saved);

        AttendanceResponseDTO result = attendanceService.checkIn(empId.toString(), new CheckInRequestDTO("Office"));

        assertNotNull(result);
        assertEquals("ATT-XYZ", result.attendanceId());
        assertEquals("Office", result.location());

        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void checkIn_employeeNotFound_throws() {
        UUID empId = UUID.randomUUID();
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(EmployeeNotFoundException.class, () ->
                attendanceService.checkIn(empId.toString(), new CheckInRequestDTO("Office"))
        );

        assertEquals(ErrorConstants.EMPLOYEE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void checkOut_noActiveAttendance_throws() {
        UUID empId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(empId);
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findLatestByEmployeeAndStatus(eq(empId), eq(AttendanceStatus.ACTIVE))).thenReturn(List.of());

        assertThrows(ActiveAttendanceNotFoundException.class, () ->
                attendanceService.checkOut(empId.toString())
        );
    }
}

