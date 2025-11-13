package com.QuanTech.QuanTech.dto.shift;

import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.constants.enums.ShiftType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamShiftTableRowDTO(
        UUID id,
        String shiftId,
        String employeeName,
        OffsetDateTime shiftDate,
        OffsetDateTime shiftStartTime,
        OffsetDateTime shiftEndTime,
        ShiftType shiftType,
        String shiftLocation,
        ShiftStatus shiftStatus
) {
}
