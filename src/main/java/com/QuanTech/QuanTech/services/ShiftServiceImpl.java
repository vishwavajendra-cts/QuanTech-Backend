package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.dto.shift.CreateNewShiftDTO;
import com.QuanTech.QuanTech.dto.shift.CreateShiftDateRequestDTO;
import com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamShiftTableRowDTO;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;

import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.ShiftNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.TeamRepository;
import com.QuanTech.QuanTech.repository.projections.shift.ShiftView;
import com.QuanTech.QuanTech.services.interfaces.ShiftService;
import com.QuanTech.QuanTech.util.DateRange;
import com.QuanTech.QuanTech.util.NanoIdGenerator;
import com.QuanTech.QuanTech.util.mappers.ShiftMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ShiftServiceImpl(
            ShiftRepository shiftRepository,
            TeamRepository teamRepository,
            EmployeeRepository employeeRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public ShiftResponseDTO createShift(CreateShiftDateRequestDTO shiftDTO, String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        Team team = teamRepository.findByTeamManagerId(mngID)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.MANAGER_TEAM_NOT_FOUND));

        Employee employee = employeeRepository.findById(shiftDTO.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_WITH_NO_TEAM));

        // if the emp does not belong to the team
        if (employee.getTeam() == null || !employee.getTeam().getId().equals(team.getId())) {
            throw new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_IN_MANAGER_TEAM);
        }

        // have to convert it from iso8601 date
        // to offsetdatetime
        ZoneId zone = ZoneId.systemDefault();

        OffsetDateTime shiftDate = shiftDTO.shiftDate().atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime shiftStart = shiftDTO.shiftDate().atTime(shiftDTO.shiftStartTime()).atZone(zone).toOffsetDateTime();
        OffsetDateTime shiftEnd = shiftDTO.shiftDate().atTime(shiftDTO.shiftEndTime()).atZone(zone).toOffsetDateTime();

        if(shiftEnd.isBefore(shiftStart)) {
            throw new IllegalArgumentException(ErrorConstants.INVALID_SHIFT_TIMING);
        }

        CreateNewShiftDTO offsetShiftDTO = new CreateNewShiftDTO(
                shiftDTO.employeeId(),
                shiftDate,
                shiftStart,
                shiftEnd,
                ShiftStatus.CONFIRMED,
                shiftDTO.shiftType(),
                shiftDTO.shiftLocation()
        );

        Shift shift = new Shift();

        int shiftIdLength = 10;
        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                shiftIdLength
        );

        shift.setPublicId("SH-" + nanoId);
        shift.setEmployee(employee);
        shift.setShiftDate(offsetShiftDTO.shiftDate());
        shift.setShiftStartTime(offsetShiftDTO.shiftStartTime());
        shift.setShiftEndTime(offsetShiftDTO.shiftEndTime());
        shift.setShiftType(offsetShiftDTO.shiftType());
        shift.setShiftStatus(offsetShiftDTO.shiftStatus());
        shift.setShiftLocation(offsetShiftDTO.shiftLocation());

        Shift savedShift = shiftRepository.save(shift);

        return ShiftMapper.shiftEntityToDto(savedShift);
    }

    @Override
    public List<ShiftResponseDTO> getEmployeeShifts(String employeeId) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        return shiftRepository.findShiftViewByEmployeeId(empID)
                .orElseThrow(() -> new ShiftNotFoundException(ErrorConstants.SHIFT_NOT_FOUND));
    }

    @Override
    public List<ShiftResponseDTO> getTeamsShiftByManager(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        List<UUID> employeeIDs = teamRepository.findEmployeeIdsByManagerId(mngID);

        if (employeeIDs.isEmpty()) {
            return List.of(); // returning an empty list
        }

        List<ShiftView> shifts = shiftRepository.findMultipleShiftViewByEmployeeId(employeeIDs);

        return shifts.stream()
                .map(shift -> ShiftMapper.shiftViewToDto(shift))
                .toList();

    }

    @Override
    public List<TeamShiftTableRowDTO> getTeamShiftsByManagerAndDatePicker(String managerId, LocalDate date) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        List<UUID> employeeIds = teamRepository.findEmployeeIdsByManagerId(mngID);

        if (employeeIds.isEmpty()) {
            throw new ResourceNotFoundException(ErrorConstants.MANAGER_WITH_NO_TEAM);
        }

        DateRange.DateRangeRecord range = DateRange.forLocalDate(date, ZoneId.systemDefault());
        OffsetDateTime start = range.start();
        OffsetDateTime end = range.end();

        return shiftRepository.findTeamShiftRowByEmployeeIdsAndDate(employeeIds, start, end);
    }
}
