package com.QuanTech.QuanTech.repository.projections.shift;

import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.constants.enums.ShiftType;

import java.time.OffsetDateTime;

public interface TeamShiftRowView {
    String getEmployeeFirstName();

    String getEmployeeLastName();

    OffsetDateTime getShiftDate();

    OffsetDateTime getShiftStartTime();

    OffsetDateTime getShiftEndTime();

    ShiftType getShiftType();

    String getShiftLocation();

    ShiftStatus getShiftStatus();
}
