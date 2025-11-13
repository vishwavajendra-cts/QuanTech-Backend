package com.QuanTech.QuanTech.testutil;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.TeamDTO;
import com.QuanTech.QuanTech.dto.shift.ShiftCardDTO;
import com.QuanTech.QuanTech.dto.shift.TeamEmployeesShiftFormResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamMembersShiftDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Test data initializer for Team-related tests.
 * Provides factory methods to create consistent test data across different test cases.
 */
public class TeamTestDataInitializer {

    // Team Data
    public static TeamDTO createTeamDTO() {
        UUID managerId = UUID.randomUUID();
        List<UUID> employeeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        return new TeamDTO("TEAM-001", "Development Team", managerId, employeeIds);
    }

    public static TeamDTO createTeamDTO(String teamId, String teamName, UUID managerId, List<UUID> employeeIds) {
        return new TeamDTO(teamId, teamName, managerId, employeeIds);
    }

    // Employee Data
    public static EmployeeDTO createEmployeeDTO(String displayId, String firstName, String lastName) {
        return new EmployeeDTO(
                UUID.randomUUID(),
                displayId,
                firstName,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com",
                null,
                "1234567890",
                "Developer",
                true,
                "Engineering",
                null,
                null
        );
    }

    public static EmployeeDTO createEmployeeDTO(UUID id, String displayId, String firstName, String lastName,
                                                 String email, String phoneNumber, String designation) {
        return new EmployeeDTO(
                id,
                displayId,
                firstName,
                lastName,
                email,
                null,
                phoneNumber,
                designation,
                true,
                "Engineering",
                null,
                null
        );
    }

    public static List<EmployeeDTO> createTeamMembers() {
        EmployeeDTO e1 = new EmployeeDTO(
                UUID.randomUUID(),
                "EMP-001",
                "John",
                "Doe",
                "john.doe@example.com",
                null,
                "1234567890",
                "Developer",
                true,
                "Engineering",
                null,
                null
        );

        EmployeeDTO e2 = new EmployeeDTO(
                UUID.randomUUID(),
                "EMP-002",
                "Jane",
                "Smith",
                "jane.smith@example.com",
                null,
                "0987654321",
                "Tester",
                true,
                "Engineering",
                null,
                null
        );

        return List.of(e1, e2);
    }

    // Shift Data
    public static ShiftCardDTO createShiftCardDTO() {
        return new ShiftCardDTO(
                UUID.randomUUID(),
                "SHIFT-001",
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0),
                OffsetDateTime.now().plusDays(1).withHour(17).withMinute(0).withSecond(0).withNano(0),
                "Office A",
                null,
                null
        );
    }

    public static ShiftCardDTO createShiftCardDTO(String shiftId, String location, int daysFromNow) {
        OffsetDateTime shiftDate = OffsetDateTime.now().plusDays(daysFromNow);
        return new ShiftCardDTO(
                UUID.randomUUID(),
                shiftId,
                shiftDate,
                shiftDate.withHour(9).withMinute(0).withSecond(0).withNano(0),
                shiftDate.withHour(17).withMinute(0).withSecond(0).withNano(0),
                location,
                null,
                null
        );
    }

    public static ShiftCardDTO createShiftCardDTO(UUID id, String shiftId, OffsetDateTime shiftDate,
                                                   OffsetDateTime startTime, OffsetDateTime endTime, String location) {
        return new ShiftCardDTO(id, shiftId, shiftDate, startTime, endTime, location, null, null);
    }

    // Team Members with Shifts
    public static TeamMembersShiftDTO createTeamMembersShiftDTO(String firstName, String lastName) {
        ShiftCardDTO shift = createShiftCardDTO();
        return new TeamMembersShiftDTO(UUID.randomUUID(), firstName, lastName, List.of(shift));
    }

    public static TeamMembersShiftDTO createTeamMembersShiftDTO(UUID id, String firstName, String lastName, List<ShiftCardDTO> shifts) {
        return new TeamMembersShiftDTO(id, firstName, lastName, shifts);
    }

    public static List<TeamMembersShiftDTO> createTeamMembersWithShifts() {
        ShiftCardDTO shift = createShiftCardDTO();
        TeamMembersShiftDTO member = new TeamMembersShiftDTO(
                UUID.randomUUID(),
                "Alice",
                "Johnson",
                List.of(shift)
        );
        return List.of(member);
    }

    // Team Employees for Shift Form
    public static TeamEmployeesShiftFormResponseDTO createTeamEmployeesShiftFormResponseDTO(String firstName, String lastName) {
        return new TeamEmployeesShiftFormResponseDTO(UUID.randomUUID(), firstName, lastName);
    }

    public static TeamEmployeesShiftFormResponseDTO createTeamEmployeesShiftFormResponseDTO(UUID id, String firstName, String lastName) {
        return new TeamEmployeesShiftFormResponseDTO(id, firstName, lastName);
    }

    public static List<TeamEmployeesShiftFormResponseDTO> createTeamEmployeesShiftForm() {
        TeamEmployeesShiftFormResponseDTO r1 = new TeamEmployeesShiftFormResponseDTO(
                UUID.randomUUID(),
                "Bob",
                "Wilson"
        );
        TeamEmployeesShiftFormResponseDTO r2 = new TeamEmployeesShiftFormResponseDTO(
                UUID.randomUUID(),
                "Carol",
                "Brown"
        );
        return List.of(r1, r2);
    }
}

