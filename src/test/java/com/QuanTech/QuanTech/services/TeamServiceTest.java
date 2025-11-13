package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.TeamDTO;
import com.QuanTech.QuanTech.dto.shift.TeamEmployeesShiftFormResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamMembersShiftDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.TeamRepository;
import com.QuanTech.QuanTech.repository.projections.shift.EmployeeShiftView;
import com.QuanTech.QuanTech.services.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private EmployeeRepository employeeRepository;
    private ShiftRepository shiftRepository;
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        teamRepository = mock(TeamRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        teamService = new TeamServiceImpl(teamRepository, employeeRepository, shiftRepository);
    }

    @Test
    void createTeam_savesTeamAndEmployees() {
        UUID managerId = UUID.randomUUID();
        UUID emp1 = UUID.randomUUID();
        UUID emp2 = UUID.randomUUID();
        TeamDTO dto = new TeamDTO("T-1", "Team One", managerId, List.of(emp1, emp2));

        Employee manager = new Employee();
        manager.setId(managerId);

        Employee e1 = new Employee();
        e1.setId(emp1);
        Employee e2 = new Employee();
        e2.setId(emp2);

        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(employeeRepository.findById(emp1)).thenReturn(Optional.of(e1));
        when(employeeRepository.findById(emp2)).thenReturn(Optional.of(e2));

        TeamServiceImpl service = new TeamServiceImpl(teamRepository, employeeRepository, shiftRepository);
        TeamDTO result = service.createTeam(dto);

        assertEquals(dto, result);
        verify(teamRepository).save(any(Team.class));
        verify(employeeRepository).saveAll(anyList());
    }

    @Test
    void getTeamMembers_returnsEmployeeDTOList() {
        UUID managerId = UUID.randomUUID();
        Team team = new Team();
        Employee e1 = new Employee();
        e1.setId(UUID.randomUUID());
        e1.setFirstName("A");
        Employee e2 = new Employee();
        e2.setId(UUID.randomUUID());
        e2.setFirstName("B");
        team.setEmployees(List.of(e1, e2));

        when(teamRepository.findByTeamManagerId(managerId)).thenReturn(Optional.of(team));

        List<EmployeeDTO> result = teamService.getTeamMembers(managerId.toString());
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).firstName());
    }

    @Test
    void getTeamSize_returnsCount() {
        UUID managerId = UUID.randomUUID();
        when(teamRepository.countTeamEmployeesByManagerId(managerId)).thenReturn(3L);

        int size = teamService.getTeamSize(managerId.toString());
        assertEquals(3, size);
    }

    @Test
    void deleteTeam_deletesIfExists() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(true);

        teamService.deleteTeam(teamId.toString());
        verify(teamRepository).deleteById(teamId);
    }

    @Test
    void getTeamMembersWithUpcomingShifts_returnsList() {
        UUID empId = UUID.randomUUID();
        Employee e1 = new Employee();
        e1.setId(UUID.randomUUID());
        e1.setFirstName("A");
        e1.setLastName("B");
        List<Employee> teamEmployees = List.of(e1);

        when(employeeRepository.findTeamEmployeesExcludingSelfAndManager(empId)).thenReturn(teamEmployees);

        EmployeeShiftView shiftView = mock(EmployeeShiftView.class);
        when(shiftView.getEmployeeId()).thenReturn(e1.getId());
        when(shiftRepository.findUpcomingShiftViewByEmployeeIds(anyList(), any())).thenReturn(List.of(shiftView));

        List<TeamMembersShiftDTO> result = teamService.getTeamMembersWithUpcomingShifts(empId.toString());
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).firstName());
    }

    @Test
    void getTeamEmployeesByManagerInCreateShiftForm_returnsList() {
        UUID managerId = UUID.randomUUID();
        TeamEmployeesShiftFormResponseDTO dto = new TeamEmployeesShiftFormResponseDTO(UUID.randomUUID(), "F", "L");
        when(teamRepository.findTeamEmployeesByManager(managerId)).thenReturn(List.of(dto));

        List<TeamEmployeesShiftFormResponseDTO> result = teamService.getTeamEmployeesByManagerInCreateShiftForm(managerId.toString());
        assertEquals(1, result.size());
        assertEquals("F", result.get(0).firstName());
    }
}