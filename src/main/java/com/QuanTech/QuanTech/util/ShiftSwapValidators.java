package com.QuanTech.QuanTech.util;

import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.ShiftSwapRequest;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.exception.custom.ShiftSwapRequestException;

public class ShiftSwapValidators {
    public static void ensureManagerOwnsTeam(Employee manager, ShiftSwapRequest req) {
        // authorize if the manager manages the requester's team or the requested's team
        Team requesterTeam = req.getRequester() != null ? req.getRequester().getTeam() : null;
        Team requestedTeam = req.getRequested() != null ? req.getRequested().getTeam() : null;

        boolean authorized = false;

        if (requesterTeam != null && requesterTeam.getTeamManager() != null
                && manager.getId().equals(requesterTeam.getTeamManager().getId())) {
            authorized = true;
        }

        if (!authorized && requestedTeam != null && requestedTeam.getTeamManager() != null
                && manager.getId().equals(requestedTeam.getTeamManager().getId())) {
            authorized = true;
        }

        if (!authorized) {
            throw new ShiftSwapRequestException(ErrorConstants.EMPLOYEE_NOT_IN_MANAGER_TEAM);
        }
    }

    public static void ensurePending(ShiftSwapRequest shiftSwapRequest) {
        if (shiftSwapRequest.getStatus() != ShiftSwapRequestStatus.PENDING) {
            throw new ShiftSwapRequestException(ErrorConstants.PENDING_REQUEST_HANDLE_ONLY);
        }
    }
}
