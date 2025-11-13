package com.QuanTech.QuanTech.dto.shiftSwapRequest;

import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShiftSwapResponseDTO(
        UUID id,
        String shiftSwapId,
        String fromEmployeeName,
        String toEmployeeName,
        ShiftSwapRequestStatus status,
        ShiftInfo offeringShift,
        ShiftInfo requestingShift,
        String reason,
        String approvedByName,
        OffsetDateTime approvedDate
) {
    public static record ShiftInfo(
            UUID id,
            String shiftType,
            OffsetDateTime shiftDate,
            OffsetDateTime shiftStartTime,
            OffsetDateTime shiftEndTime,
            String shiftLocation
    ) {}
}
