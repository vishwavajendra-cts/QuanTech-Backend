package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.TeamDTO;
import com.QuanTech.QuanTech.dto.shift.TeamEmployeesShiftFormResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamMembersShiftDTO;

import java.util.List;


public interface TeamService {
    TeamDTO createTeam(TeamDTO teamDTO);

    List<EmployeeDTO> getTeamMembers(String managerId);

    int getTeamSize(String managerId);

    void deleteTeam(String teamId);

    List<TeamMembersShiftDTO> getTeamMembersWithUpcomingShifts(String employeeId);

    List<TeamEmployeesShiftFormResponseDTO> getTeamEmployeesByManagerInCreateShiftForm(String managerId);
}
