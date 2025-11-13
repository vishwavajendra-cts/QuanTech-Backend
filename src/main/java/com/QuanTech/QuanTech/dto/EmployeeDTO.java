package com.QuanTech.QuanTech.dto;

import com.QuanTech.QuanTech.constants.enums.Gender;
import com.QuanTech.QuanTech.constants.enums.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import java.util.UUID;

@Schema(
        description = "Employee DTO Model Information which acts as a request and response"
)
public record EmployeeDTO(
        @Schema(
                name = "Employee UUID",
                description = "Employee Universally Unique Identifier"
        )
        UUID id,

        @Schema(
                name = "Display ID",
                description = "A human-readable employee ID, such as 'EMP-001'."
        )
        String displayEmployeeId,

        @Schema(
                name = "First Name",
                description = "The employee's first name."
        )
        String firstName,

        @Schema(
                name = "Last Name",
                description = "The employee's last name."
        )
        String lastName,

        @Schema(
                name = "Email",
                description = "The employee's official email address. Must be a valid format."
        )
        @Email(message = "Email is not correct")
        String email,

        @Schema(
                name = "Gender",
                description = "The employee's gender.",
                example = "MALE, FEMALE"
        )
        Gender gender,

        @Schema(
                name = "Phone Number",
                description = "The employee's contact phone number."
        )
        String phoneNumber,

        @Schema(
                name = "Job Title",
                description = "The employee's current job title (e.g., 'Software Engineer')."
        )
        String jobTitle,

        @JsonAlias({"active", "isActive"})
        @Schema(
                name = "Active Status",
                description = "Indicates whether the employee is currently active (true) or terminated/inactive (false).",
                defaultValue = "true"
        )
        boolean isActive,

        @Schema(
                name = "Department Name",
                description = "The name of the department the employee belongs to (e.g., 'Engineering')."
        )
        String departmentName,

        @Schema(
                name = "Role",
                description = "The employee's organizational role ('EMPLOYEE', 'MANAGER')"
        )
        Role role,

        @Schema(
                name = "Team ID",
                description = "The UUID of the team the employee is a member of"
        )
        String teamId
) {
}
