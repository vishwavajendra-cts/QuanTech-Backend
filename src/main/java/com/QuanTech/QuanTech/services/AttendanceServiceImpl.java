package com.QuanTech.QuanTech.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;
import com.QuanTech.QuanTech.constants.enums.AttendanceStatus;
import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.CheckInRequestDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceDisplayByDateResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceRowDTO;
import com.QuanTech.QuanTech.entity.Attendance;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.exception.custom.ActiveAttendanceExistsException;
import com.QuanTech.QuanTech.exception.custom.ActiveAttendanceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.InvalidDateException;
import com.QuanTech.QuanTech.repository.AttendanceRepository;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.services.interfaces.AttendanceService;
import com.QuanTech.QuanTech.util.DateRange;
import com.QuanTech.QuanTech.util.NanoIdGenerator;
import com.QuanTech.QuanTech.util.mappers.AttendanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceResponseDTO getLatestAttendance(String employeeId) {
        log.info("Invoked the getLatestAttendance service method, employeeId:{}", employeeId);
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);
        List<AttendanceResponseDTO> list = attendanceRepository.findAllByEmployeeOrderByDateDesc(empID);

        if (!list.isEmpty()) {
            return list.getFirst();
        }
        return new AttendanceResponseDTO(
                "N/A",
                OffsetDateTime.now(),
                null,
                null,
                0.0,
                AttendanceStatus.COMPLETE,
                null
        );
    }


    @Override
    public List<AttendanceResponseDTO> getAttendanceHistory(String employeeId) {
        log.info("Invoked the getAttendanceHistory service method, employeeId:{}", employeeId);
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);
        return attendanceRepository.findAllByEmployeeOrderByDateDesc(empID);
    }

    @Override
    public AttendanceResponseDTO checkIn(String employeeId, CheckInRequestDTO checkInRequestDTO) {
        log.info("Invoked the checkIn service method, employeeId:{}, checkInRequestDTO:{}", employeeId, checkInRequestDTO);
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        Employee employee = employeeRepository.findById(empID)
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND));

        List<Attendance> activeList = attendanceRepository.findLatestByEmployeeAndStatus(empID, AttendanceStatus.ACTIVE);

        if(!activeList.isEmpty()) {
            throw new ActiveAttendanceExistsException(ErrorConstants.ALREADY_CHECKED_IN);
        }

        OffsetDateTime now = OffsetDateTime.now();

        Attendance attendance = new Attendance();

        int attendanceIdLength = 10;
        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                attendanceIdLength
        );

        attendance.setAttendanceId("ATT-"+nanoId);
        attendance.setEmployee(employee);
        attendance.setDate(now);
        attendance.setCheckIn(now);
        attendance.setCheckOut(null);
        attendance.setHoursWorked(0.0);
        attendance.setAttendanceStatus(AttendanceStatus.ACTIVE);
        attendance.setLocation(checkInRequestDTO != null ? checkInRequestDTO.location() : null);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return AttendanceMapper.attendanceEntityToDto(savedAttendance);

    }

    @Override
    public AttendanceResponseDTO checkOut(String employeeId) {
        log.info("Invoked the checkOut service method, employeeId:{}", employeeId);
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        Employee employee = employeeRepository.findById(empID)
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND));

        List<Attendance> activeList = attendanceRepository.findLatestByEmployeeAndStatus(empID, AttendanceStatus.ACTIVE);
        Attendance attendance = activeList.isEmpty() ? null : activeList.get(0);

        if(attendance == null) {
            throw new ActiveAttendanceNotFoundException(ErrorConstants.ACTIVE_ATTENDANCE_NOT_FOUND);
        }

        OffsetDateTime now = OffsetDateTime.now();
        attendance.setCheckOut(now);
        double hours = 0.0;
        if(attendance.getCheckIn() != null) {
            hours = Duration.between(attendance.getCheckIn(), now).toMinutes() / 60.0;
        }

        attendance.setHoursWorked(hours);

        attendance.setAttendanceStatus(AttendanceStatus.COMPLETE);

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceMapper.attendanceEntityToDto(saved);
    }

    @Override
    public ManagerAttendanceDisplayByDateResponseDTO getTeamsAttendanceByDate(String managerId, String date) {
        log.info("Invoked the getTeamsAttendanceByDate service method, managerId:{}, date:{}", managerId, date);
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        employeeRepository.findById(mngID)
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.MANAGER_NOT_FOUND));

        LocalDate localDate;

        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException(ErrorConstants.INVALID_DATE_FORMAT);
        }

        DateRange.DateRangeRecord dateRange = DateRange.forLocalDate(localDate, ZoneId.systemDefault());

        List<ManagerAttendanceRowDTO> rows = attendanceRepository.findTeamAttendance(mngID, dateRange.start(), dateRange.end());

        return new ManagerAttendanceDisplayByDateResponseDTO(localDate, rows);
    }
}
