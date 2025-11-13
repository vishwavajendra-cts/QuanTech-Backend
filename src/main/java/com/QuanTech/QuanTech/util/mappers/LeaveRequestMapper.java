package com.QuanTech.QuanTech.util.mappers;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.dto.leaveRequests.LeaveRequestCreateDTO;
import com.QuanTech.QuanTech.dto.leaveRequests.LeaveRequestResponseDTO;
import com.QuanTech.QuanTech.dto.leaveRequests.ManagerLeaveRequestDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LeaveRequest;
import com.QuanTech.QuanTech.util.NanoIdGenerator;

import java.time.OffsetDateTime;

import static com.QuanTech.QuanTech.util.CalculateLeaveRequestDays.getLeaveRequestDays;

public class LeaveRequestMapper {

    public static LeaveRequest leaveRequestDtoToEntity(LeaveRequestCreateDTO leaveRequestCreateDTO, Employee employee) {
        LeaveRequest leaveRequestEntity = new LeaveRequest();

        int leaveRequestIdLength = 10;

        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                leaveRequestIdLength
        );

        leaveRequestEntity.setLeaveRequestId("LR-" + nanoId);
        leaveRequestEntity.setEmployee(employee);
        leaveRequestEntity.setLeaveType(leaveRequestCreateDTO.leaveType());
        leaveRequestEntity.setStartDate(leaveRequestCreateDTO.startDate());
        leaveRequestEntity.setEndDate(leaveRequestCreateDTO.endDate());
        leaveRequestEntity.setReason(leaveRequestCreateDTO.reason());
        leaveRequestEntity.setLeaveStatus(LeaveStatus.PENDING);
        leaveRequestEntity.setRequestDate(OffsetDateTime.now());

        return leaveRequestEntity;
    }


    public static ManagerLeaveRequestDTO leaveRequestManagerEntityToDto(LeaveRequest lr) {
        return new ManagerLeaveRequestDTO(
                lr.getId(),
                lr.getEmployee().getId(),
                lr.getEmployee().getDisplayEmployeeId(),
                lr.getEmployee().getFirstName(),
                lr.getEmployee().getLastName(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                getLeaveRequestDays(lr),
                lr.getLeaveStatus(),
                lr.getReason()
        );
    }

    public static LeaveRequestResponseDTO leaveRequestEntityToResponse(LeaveRequest lr) {
        return new LeaveRequestResponseDTO(
                lr.getLeaveRequestId(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                getLeaveRequestDays(lr),
                lr.getLeaveStatus(),
                lr.getRequestDate(),
                lr.getReason()
        );
    }
}
