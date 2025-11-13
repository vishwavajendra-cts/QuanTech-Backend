package com.QuanTech.QuanTech.util.mappers;

import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.entity.ShiftSwapRequest;

import static com.QuanTech.QuanTech.util.CreateFullName.fullName;

public class  ShiftSwapMapper {

    private ShiftSwapMapper() {}

//    public static ShiftSwapQueryResponseDTO shiftSwapQueryEntityToDto(ShiftSwapRequest shiftSwapRequestEntity) {
//        return new ShiftSwapQueryResponseDTO(
//                shiftSwapRequestEntity.getId(),
//                shiftSwapRequestEntity.getPublicId(),
//                fullName(shiftSwapRequestEntity.getRequester()),
//                fullName(shiftSwapRequestEntity.getRequested()),
//                shiftSwapRequestEntity.getStatus(),
//                shiftInfoHelper(shiftSwapRequestEntity.getOfferingShift()),
//                shiftInfoHelper(shiftSwapRequestEntity.getRequestingShift()),
//                shiftSwapRequestEntity.getReason(),
//                shiftSwapRequestEntity.getApprovedBy() != null ? fullName(shiftSwapRequestEntity.getApprovedBy()) : null,
//                shiftSwapRequestEntity.getApprovedDate()
//        );
//    }

    public static ShiftSwapResponseDTO shiftSwapEntityToDto(ShiftSwapRequest shiftSwapRequestEntity) {
        return new ShiftSwapResponseDTO(
                shiftSwapRequestEntity.getId(),
                shiftSwapRequestEntity.getPublicId(),
                fullName(shiftSwapRequestEntity.getRequester()),
                fullName(shiftSwapRequestEntity.getRequested()),
                shiftSwapRequestEntity.getStatus(),
                shiftInfoHelper(shiftSwapRequestEntity.getOfferingShift()),
                shiftInfoHelper(shiftSwapRequestEntity.getRequestingShift()),
                shiftSwapRequestEntity.getReason(),
                shiftSwapRequestEntity.getApprovedBy() != null ? fullName(shiftSwapRequestEntity.getApprovedBy()) : null,
                shiftSwapRequestEntity.getApprovedDate()
        );
    }

    private static ShiftSwapResponseDTO.ShiftInfo shiftInfoHelper(Shift shift){
        return new ShiftSwapResponseDTO.ShiftInfo(
                shift.getId(),
                shift.getShiftType() != null ? shift.getShiftType().name() : null,
                shift.getShiftDate(),
                shift.getShiftStartTime(),
                shift.getShiftEndTime(),
                shift.getShiftLocation()
        );
    }
}
