package com.QuanTech.QuanTech.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;
import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceDTO;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceResponseDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LeaveBalance;
import com.QuanTech.QuanTech.exception.custom.DuplicateLeaveBalanceFound;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.LeaveBalanceNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.LeaveBalanceRepository;
import com.QuanTech.QuanTech.services.interfaces.LeaveBalanceService;
import com.QuanTech.QuanTech.util.NanoIdGenerator;
import com.QuanTech.QuanTech.util.mappers.LeaveBalanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public LeaveBalanceServiceImpl(LeaveBalanceRepository leaveBalanceRepository, EmployeeRepository employeeRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<LeaveBalanceResponseDTO> getLeaveBalancesByEmployeeId(String employeeId) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        return leaveBalanceRepository.findLeaveBalanceViewByEmployeeId(empID)
                .orElseThrow(() -> new LeaveBalanceNotFoundException(ErrorConstants.LEAVE_BALANCE_NOT_FOUND));
    }

    // for creating a leave balance
    @Override
    @Transactional
    public LeaveBalanceDTO createLeaveBalance(String employeeId, LeaveType leaveType, int leaveBalance) {
        UUID empID = parseUUID(employeeId, UuidErrorConstants.INVALID_EMPLOYEE_UUID);

        Employee employee = employeeRepository.findById(empID)
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND));

        // for duplicate leave balance and leave type present in the database
        if (leaveBalanceRepository.existsByEmployeeIdAndLeaveType(employee.getId(), leaveType)) {
            throw new DuplicateLeaveBalanceFound(ErrorConstants.LEAVE_BALANCE_ALREADY_EXISTS);
        }

        LeaveBalance lb = new LeaveBalance();
        int balanceIdLength = 10;

        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                balanceIdLength
        );

        lb.setBalanceId("lb-" + nanoId);
        lb.setEmployee(employee);
        lb.setLeaveType(leaveType);
        lb.setLeaveBalance(leaveBalance);

        LeaveBalance savedLeaveBalance = leaveBalanceRepository.save(lb);

        return LeaveBalanceMapper.leaveBalanceEntityToDTO(savedLeaveBalance);
    }
}
