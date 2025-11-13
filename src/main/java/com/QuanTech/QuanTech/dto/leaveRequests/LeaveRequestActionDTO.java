package com.QuanTech.QuanTech.dto.leaveRequests;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;


public record LeaveRequestActionDTO(
        @NotNull(message = "Please enter a valid action [APPROVED | REJECTED]")
        LeaveStatus action
) {
}
