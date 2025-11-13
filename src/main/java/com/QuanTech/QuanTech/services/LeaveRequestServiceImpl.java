package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;
import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.dto.leaveRequests.*;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LeaveBalance;
import com.QuanTech.QuanTech.entity.LeaveRequest;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.InvalidLeaveRequestException;
import com.QuanTech.QuanTech.exception.custom.LeaveBalanceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.LeaveRequestNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.LeaveBalanceRepository;
import com.QuanTech.QuanTech.repository.LeaveRequestRepository;
import com.QuanTech.QuanTech.services.interfaces.LeaveRequestService;
import com.QuanTech.QuanTech.util.mappers.LeaveRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository, LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Transactional
    @Override
    public LeaveRequestResponseDTO createLeaveRequest(String employeeId, LeaveRequestCreateRequestDTO request) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        Employee employee = employeeRepository.findById(empID)
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND + empID));

        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidLeaveRequestException(ErrorConstants.INVALID_LEAVE_REQUESTS);
        }

//        ZoneId zone = ZoneId.systemDefault();

//        OffsetDateTime startDate = request.startDate().atStartOfDay(zone).toOffsetDateTime();
//        OffsetDateTime endDate = request.endDate().plusDays(0).atStartOfDay(zone).toOffsetDateTime();

        LeaveRequestCreateDTO leaveRequestCreateDTO = new LeaveRequestCreateDTO(
                request.leaveType(),
                request.startDate(),
                request.endDate(),
                request.reason()
        );


        LeaveRequest leaveRequestEntity = LeaveRequestMapper.leaveRequestDtoToEntity(leaveRequestCreateDTO, employee);

        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequestEntity);

        return LeaveRequestMapper.leaveRequestEntityToResponse(savedLeaveRequest);
    }

    @Override
    public List<LeaveRequestResponseDTO> getEmployeeLeaveRequests(String employeeId) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        List<LeaveRequest> leaveRequests = leaveRequestRepository.findLeaveRequestsByEmployeeId(empID);

        List<LeaveRequestResponseDTO> response = new ArrayList<>();

        for (LeaveRequest lr : leaveRequests) {
            LeaveRequestResponseDTO singleLeaveRequestDto = LeaveRequestMapper.leaveRequestEntityToResponse(lr);
            response.add(singleLeaveRequestDto);
        }

        return response;
    }

    @Override
    public List<ManagerLeaveRequestDTO> getTeamLeaveRequests(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

        List<LeaveRequest> teamsLeaveRequests = leaveRequestRepository.findTeamLeaveRequests(mngID);
        return teamsLeaveRequests.stream()
                .map(LeaveRequestMapper::leaveRequestManagerEntityToDto)
                .toList();
    }

    @Override
    @Transactional
    public void actionOnLeaveRequest(String managerId, String requestId, LeaveRequestActionDTO leaveRequestActionDTO) {
        UUID leaveRequestID = parseUUID(requestId, UuidErrorConstants.INVALID_LEAVE_REQUEST_ID);

        LeaveRequest lr = leaveRequestRepository.findById(leaveRequestID)
                .orElseThrow(() -> new LeaveRequestNotFoundException(ErrorConstants.LEAVE_REQUEST_NOT_FOUND));

        if (lr.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException(ErrorConstants.LEAVE_REQUEST_ALREADY_PROCESSED);
        }

        LeaveStatus action = leaveRequestActionDTO.action();

        if (action == LeaveStatus.APPROVED) {
            long daysInclusive = ChronoUnit.DAYS.between(
                    lr.getStartDate(), lr.getEndDate()
            ) + 1;

            if (daysInclusive <= 0) {
                throw new InvalidLeaveRequestException(ErrorConstants.INVALID_LEAVE_REQUESTS);
            }

            LeaveBalance lb = leaveBalanceRepository.findByEmployeeIdAndLeaveType(lr.getEmployee().getId(), lr.getLeaveType())
                    .orElseThrow(() -> new LeaveBalanceNotFoundException(ErrorConstants.LEAVE_BALANCE_NOT_FOUND));

            int required = Math.toIntExact(daysInclusive);

            int current = lb.getLeaveBalance();

            // checking if the current leave balances I got are less than the ones im requesting
            if (current < required) {
                throw new IllegalStateException(ErrorConstants.INSUFFICIENT_LEAVE_BALANCE);
            }

            lb.setLeaveBalance(current - required);

            leaveBalanceRepository.save(lb);
        }

        lr.setLeaveStatus(leaveRequestActionDTO.action());
        leaveRequestRepository.save(lr);
    }

    @Override
    public ManagerLeaveRequestDataDTO getLeaveRequestsStatsByManager(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);
        LocalDate now = LocalDate.now();

        long pending = leaveRequestRepository.countByManagerAndStatus(mngID, LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByManagerAndStatus(mngID, LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByManagerAndStatus(mngID, LeaveStatus.REJECTED);
        long onLeaveToday = leaveRequestRepository.countByOnLeaveToday(mngID, now);

        return new ManagerLeaveRequestDataDTO(pending, approved, rejected, onLeaveToday);
    }

    @Override
    public List<ManagerLeaveRequestDashboardResponseDTO> getLeaveRequestManagerDashboard(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);
        return leaveRequestRepository.leaveRequestManagerDashboard(mngID);
    }

    @Override
    public List<EmployeeLeaveRequestDashboardResponseDTO> getLeaveRequestEmployeeDashboard(String employeeId) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);
        return leaveRequestRepository.leaveRequestEmployeeDashboard(empID);
    }
}
