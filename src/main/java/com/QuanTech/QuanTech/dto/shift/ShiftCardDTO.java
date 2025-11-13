package com.QuanTech.QuanTech.dto.shift;

import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.constants.enums.ShiftType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShiftCardDTO(
        UUID id,
        String shiftId,
        OffsetDateTime shiftDate,
        OffsetDateTime shiftStartTime,
        OffsetDateTime shiftEndTime,
        String shiftLocation,
        ShiftType shiftType,
        ShiftStatus shiftStatus
) {
}
