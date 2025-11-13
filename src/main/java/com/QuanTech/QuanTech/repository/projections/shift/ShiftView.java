package com.QuanTech.QuanTech.repository.projections.shift;

import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.constants.enums.ShiftType;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ShiftView {
    UUID getId();

    String getShiftId();

    String getEmployeeId();

    OffsetDateTime getShiftDate();

    OffsetDateTime getShiftStartTime();

    OffsetDateTime getShiftEndTime();

    ShiftStatus getShiftStatus();

    ShiftType getShiftType();

    String getShiftLocation();
}
