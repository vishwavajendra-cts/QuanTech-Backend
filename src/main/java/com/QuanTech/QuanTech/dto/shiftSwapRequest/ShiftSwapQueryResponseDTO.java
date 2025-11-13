package com.QuanTech.QuanTech.dto.shiftSwapRequest;

import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShiftSwapQueryResponseDTO(
        UUID id,
        String shiftSwapId,
        String fromEmployeeName,
        String toEmployeeName,
        ShiftSwapRequestStatus status,

        // offeringShift
        String offeringShiftType,
        OffsetDateTime offeringShiftDate,
        OffsetDateTime offeringShiftStartTime,
        OffsetDateTime offeringShiftEndTime,
        String offeringShiftLocation,

        // requestingShift
        String requestingShiftType,
        OffsetDateTime requestingShiftDate,
        OffsetDateTime requestingShiftStartTime,
        OffsetDateTime requestingShiftEndTime,
        String requestingShiftLocation,

        String reason,
        String approvedByName,
        OffsetDateTime approvedDate
) {
}
