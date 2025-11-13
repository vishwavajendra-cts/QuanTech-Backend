package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;
import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.TeamDTO;
import com.QuanTech.QuanTech.dto.shift.ShiftCardDTO;
import com.QuanTech.QuanTech.dto.shift.TeamEmployeesShiftFormResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamMembersShiftDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.TeamRepository;
import com.QuanTech.QuanTech.repository.projections.shift.EmployeeShiftView;
import com.QuanTech.QuanTech.services.interfaces.TeamService;
import com.QuanTech.QuanTech.util.mappers.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, EmployeeRepository employeeRepository, ShiftRepository shiftRepository) {
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    @Transactional
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = new Team();
        team.setTeamId(teamDTO.teamId());
        team.setTeamName(teamDTO.teamName());

        Employee manager = employeeRepository.findById(teamDTO.teamManagersId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.MANAGER_NOT_FOUND));

        team.setTeamManager(manager);
//        Team savedTeam = teamRepository.save(team);
//        manager.setTeam(savedTeam);

        List<Employee> employees = new ArrayList<>();
        for (UUID employeeId : teamDTO.employeeIds()) {
            Employee empl = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND + employeeId));

            empl.setTeam(team);
            employees.add(empl);
        }
        team.setEmployees(employees);

//        employeeRepository.save(manager);

        teamRepository.save(team);
        employeeRepository.saveAll(employees);
        return teamDTO;
    }

    @Override
    public List<EmployeeDTO> getTeamMembers(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        Team team = teamRepository.findByTeamManagerId(mngID)
                .orElseThrow(() -> new RuntimeException(ErrorConstants.MANAGER_WITH_NO_TEAM + managerId));

        return team.getEmployees()
                .stream()
                .map(EmployeeMapper::employeeEntityToDto)
                .toList();
    }

    @Override
    public int getTeamSize(String managerId){
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        long getTeamCount = teamRepository.countTeamEmployeesByManagerId(mngID);

        return Math.toIntExact(getTeamCount);
    }

    @Override
    @Transactional
    public void deleteTeam(String teamId){
        UUID teamID = parseUUID(teamId, UuidErrorConstants.INVALID_TEAM_ID);

        if(!teamRepository.existsById(teamID)) {
            throw new ResourceNotFoundException(ErrorConstants.EMPLOYEE_WITH_NO_TEAM);
        }

        teamRepository.deleteById(teamID);
    }

    @Override
    public List<TeamMembersShiftDTO> getTeamMembersWithUpcomingShifts(String employeeId){
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        var teamEmployees = employeeRepository.findTeamEmployeesExcludingSelfAndManager(empID);
        if(teamEmployees.isEmpty()) {
            return List.of();
        }

        var teamEmpIds = teamEmployees.stream().map(p -> p.getId()).toList();

        var now = OffsetDateTime.now();

        List<EmployeeShiftView> shifts = shiftRepository.findUpcomingShiftViewByEmployeeIds(teamEmpIds, now);

        Map<UUID, List<ShiftCardDTO>> shiftsByEmp = shifts.stream()
                .collect(Collectors.groupingBy(
                        EmployeeShiftView::getEmployeeId,
                        Collectors.mapping(sv -> new ShiftCardDTO(
                                sv.getId()  ,
                                sv.getShiftId(),
                                sv.getShiftDate(),
                                sv.getShiftStartTime(),
                                sv.getShiftEndTime(),
                                sv.getShiftLocation(),
                                sv.getShiftType(),
                                sv.getShiftStatus()
                        ), Collectors.toList())
                ));

        return teamEmployees.stream()
                .map(p -> new TeamMembersShiftDTO(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        shiftsByEmp.getOrDefault(p.getId(), List.of())
                ))
                .sorted(Comparator.comparing(TeamMembersShiftDTO::firstName).thenComparing(TeamMembersShiftDTO::lastName))
                .toList();
    }

    @Override
    public List<TeamEmployeesShiftFormResponseDTO> getTeamEmployeesByManagerInCreateShiftForm(String managerId) {
        UUID managerID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        return teamRepository.findTeamEmployeesByManager(managerID);
    }
}
