package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.dto.shift.CreateShiftDateRequestDTO;
import com.QuanTech.QuanTech.repository.projections.shift.ShiftView;
import com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamShiftTableRowDTO;
import com.QuanTech.QuanTech.services.ShiftServiceImpl;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.ShiftNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.TeamRepository;
import com.QuanTech.QuanTech.services.interfaces.ShiftService;
import com.QuanTech.QuanTech.util.mappers.ShiftMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {

    private ShiftRepository shiftRepository;
    private TeamRepository teamRepository;
    private EmployeeRepository employeeRepository;
    private ShiftService shiftService;

    @BeforeEach
    public void setup() {
        shiftRepository = mock(ShiftRepository.class);
        teamRepository = mock(TeamRepository.class);
        employeeRepository = mock(EmployeeRepository.class);

        shiftService = new ShiftServiceImpl(
                shiftRepository, teamRepository, employeeRepository
        );
    }

    @Test
    public void createShift_success() {
        String managerId = UUID.randomUUID().toString();
        UUID managerUUID = UUID.fromString(managerId);

        // Mock team and employee
        Team team = mock(Team.class);
        Employee employee = mock(Employee.class);
        UUID employeeId = UUID.randomUUID();
        when(teamRepository.findByTeamManagerId(managerUUID)).thenReturn(Optional.of(team));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employee.getTeam()).thenReturn(team);
        when(team.getId()).thenReturn(UUID.randomUUID());

        // Mock request (use a mock to avoid depending on constructor signature)
        var createReq = mock(CreateShiftDateRequestDTO.class);
        when(createReq.employeeId()).thenReturn(employeeId);
        when(createReq.shiftDate()).thenReturn(LocalDate.now());
        when(createReq.shiftStartTime()).thenReturn(LocalTime.of(9, 0));
        when(createReq.shiftEndTime()).thenReturn(LocalTime.of(17, 0));
        when(createReq.shiftType()).thenReturn(null);
        when(createReq.shiftLocation()).thenReturn("Office");

        Shift savedShift = mock(Shift.class);
        when(shiftRepository.save(any(Shift.class))).thenReturn(savedShift);

        ShiftResponseDTO expectedDto = new ShiftResponseDTO(
                UUID.randomUUID(),
                "SH-ABC",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                null,
                "Office"
        );

        try (MockedStatic<ShiftMapper> sm = Mockito.mockStatic(ShiftMapper.class)) {
            sm.when(() -> ShiftMapper.shiftEntityToDto(savedShift)).thenReturn(expectedDto);

            ShiftResponseDTO result = shiftService.createShift(createReq, managerId);

            assertEquals(expectedDto, result);
            verify(teamRepository, times(1)).findByTeamManagerId(managerUUID);
            verify(employeeRepository, times(1)).findById(employeeId);
            verify(shiftRepository, times(1)).save(any(Shift.class));
        }
    }

    @Test
    public void createShift_managerTeamNotFound_throws() {
        String managerId = UUID.randomUUID().toString();
        UUID managerUUID = UUID.fromString(managerId);

        when(teamRepository.findByTeamManagerId(managerUUID)).thenReturn(Optional.empty());

        var createReq = mock(CreateShiftDateRequestDTO.class);

        assertThrows(ResourceNotFoundException.class, () -> shiftService.createShift(createReq, managerId));
        verify(teamRepository, times(1)).findByTeamManagerId(managerUUID);
        verifyNoInteractions(shiftRepository);
    }

    @Test
    public void createShift_employeeNotInTeam_throws() {
        String managerId = UUID.randomUUID().toString();
        UUID managerUUID = UUID.fromString(managerId);

        Team team = mock(Team.class);
        when(teamRepository.findByTeamManagerId(managerUUID)).thenReturn(Optional.of(team));

        Employee employee = mock(Employee.class);
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employee.getTeam()).thenReturn(null); // not in any team

        var createReq = mock(CreateShiftDateRequestDTO.class);

        lenient().when(createReq.employeeId()).thenReturn(employeeId);
        lenient().when(createReq.shiftDate()).thenReturn(LocalDate.now());
        lenient().when(createReq.shiftStartTime()).thenReturn(LocalTime.of(9, 0));
        lenient().when(createReq.shiftEndTime()).thenReturn(LocalTime.of(17, 0));
        lenient().when(createReq.shiftType()).thenReturn(null);
        lenient().when(createReq.shiftLocation()).thenReturn("Office");

        assertThrows(ResourceNotFoundException.class, () -> shiftService.createShift(createReq, managerId));
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(shiftRepository);
    }


    @Test
    public void getEmployeeShifts_success() {
        String empIdStr = UUID.randomUUID().toString();
        UUID empId = UUID.fromString(empIdStr);

        ShiftResponseDTO dto = new ShiftResponseDTO(UUID.randomUUID(), "SH-1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), null, null, "Loc");
        when(shiftRepository.findShiftViewByEmployeeId(empId)).thenReturn(Optional.of(List.of(dto)));

        List<ShiftResponseDTO> result = shiftService.getEmployeeShifts(empIdStr);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(shiftRepository, times(1)).findShiftViewByEmployeeId(empId);
    }

    @Test
    public void getEmployeeShifts_notFound_throws() {
        String empIdStr = UUID.randomUUID().toString();
        UUID empId = UUID.fromString(empIdStr);

        when(shiftRepository.findShiftViewByEmployeeId(empId)).thenReturn(Optional.empty());

        assertThrows(ShiftNotFoundException.class, () -> shiftService.getEmployeeShifts(empIdStr));
        verify(shiftRepository, times(1)).findShiftViewByEmployeeId(empId);
    }

    @Test
    public void getTeamsShiftByManager_success_and_emptyListWhenNoShifts() {
        String managerIdStr = UUID.randomUUID().toString();
        UUID managerId = UUID.fromString(managerIdStr);

        UUID emp1 = UUID.randomUUID();
        when(teamRepository.findEmployeeIdsByManagerId(managerId)).thenReturn(List.of(emp1));
        when(shiftRepository.findMultipleShiftViewByEmployeeId(List.of(emp1))).thenReturn(List.of()); // no shift views

        List<ShiftResponseDTO> result = shiftService.getTeamsShiftByManager(managerIdStr);
        assertEquals(0, result.size());
        verify(teamRepository, times(1)).findEmployeeIdsByManagerId(managerId);
        verify(shiftRepository, times(1)).findMultipleShiftViewByEmployeeId(List.of(emp1));
    }

    @Test
    public void getTeamsShiftByManager_mapsShiftViews() {
        String managerIdStr = UUID.randomUUID().toString();
        UUID managerId = UUID.fromString(managerIdStr);

        UUID emp1 = UUID.randomUUID();
        when(teamRepository.findEmployeeIdsByManagerId(managerId)).thenReturn(List.of(emp1));

        var shiftView = mock(ShiftView.class);
        when(shiftRepository.findMultipleShiftViewByEmployeeId(List.of(emp1))).thenReturn(List.of(shiftView));

        ShiftResponseDTO mappedDto = new ShiftResponseDTO(UUID.randomUUID(), "SH-9", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), null, null, "L");

        try (MockedStatic<ShiftMapper> sm = Mockito.mockStatic(ShiftMapper.class)) {
            sm.when(() -> ShiftMapper.shiftViewToDto(shiftView)).thenReturn(mappedDto);

            List<ShiftResponseDTO> result = shiftService.getTeamsShiftByManager(managerIdStr);

            assertEquals(1, result.size());
            assertEquals(mappedDto, result.get(0));
        }
    }

    @Test
    public void getTeamShiftsByManagerAndDatePicker_success() {
        String managerIdStr = UUID.randomUUID().toString();
        UUID managerId = UUID.fromString(managerIdStr);

        UUID emp1 = UUID.randomUUID();
        when(teamRepository.findEmployeeIdsByManagerId(managerId)).thenReturn(List.of(emp1));

        LocalDate date = LocalDate.now();
        OffsetDateTime start = date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        TeamShiftTableRowDTO row = new TeamShiftTableRowDTO(UUID.randomUUID(), "SH-1", "John Doe", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), null, "Loc", null);
        when(shiftRepository.findTeamShiftRowByEmployeeIdsAndDate(eq(List.of(emp1)), any(), any())).thenReturn(List.of(row));

        List<TeamShiftTableRowDTO> result = shiftService.getTeamShiftsByManagerAndDatePicker(managerIdStr, date);

        assertEquals(1, result.size());
        assertEquals(row, result.get(0));
    }

    @Test
    public void getTeamShiftsByManagerAndDatePicker_noEmployees_throws() {
        String managerIdStr = UUID.randomUUID().toString();
        UUID managerId = UUID.fromString(managerIdStr);

        when(teamRepository.findEmployeeIdsByManagerId(managerId)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> shiftService.getTeamShiftsByManagerAndDatePicker(managerIdStr, LocalDate.now()));
        verify(teamRepository, times(1)).findEmployeeIdsByManagerId(managerId);
    }
}