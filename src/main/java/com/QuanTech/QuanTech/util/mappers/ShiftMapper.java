package com.QuanTech.QuanTech.util.mappers;

import com.QuanTech.QuanTech.dto.shift.CreateNewShiftDTO;
import com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.repository.projections.shift.ShiftView;

public class ShiftMapper {
    public static ShiftResponseDTO shiftEntityToDto(Shift shift) {
        return new ShiftResponseDTO(
                shift.getId(),
                shift.getPublicId(),
                shift.getShiftDate(),
                shift.getShiftStartTime(),
                shift.getShiftEndTime(),
                shift.getShiftStatus(),
                shift.getShiftType(),
                shift.getShiftLocation()
        );
    }

    public static CreateNewShiftDTO createShiftEntityToDto(Shift shift) {
        return new CreateNewShiftDTO(
                shift.getEmployee().getId(),
                shift.getShiftDate(),
                shift.getShiftStartTime(),
                shift.getShiftEndTime(),
                shift.getShiftStatus(),
                shift.getShiftType(),
                shift.getShiftLocation()
        );
    }

    public static ShiftResponseDTO shiftViewToDto(ShiftView view) {
        if(view == null) return null;

        return new ShiftResponseDTO(
                view.getId(),
                view.getShiftId(),
                view.getShiftDate(),
                view.getShiftStartTime(),
                view.getShiftEndTime(),
                view.getShiftStatus(),
                view.getShiftType(),
                view.getShiftLocation()
        );
    }
}
