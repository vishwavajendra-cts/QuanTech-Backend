package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceDTO;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceResponseDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LeaveBalance;
import com.QuanTech.QuanTech.exception.custom.DuplicateLeaveBalanceFound;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.LeaveBalanceNotFoundException;
import com.QuanTech.QuanTech.repository.LeaveBalanceRepository;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.services.LeaveBalanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveBalanceServiceImpl leaveBalanceService;

    @Captor
    private ArgumentCaptor<LeaveBalance> leaveBalanceCaptor;

    private UUID employeeUuid;
    private String employeeIdStr;

    @BeforeEach
    void setUp() {
        employeeUuid = UUID.randomUUID();
        employeeIdStr = employeeUuid.toString();
    }

    @Test
    void getLeaveBalancesByEmployeeId_returnsList_whenFound() {
        // prepare
        LeaveBalanceResponseDTO dto1 = new LeaveBalanceResponseDTO("lb-1", LeaveType.VACATION, 10);
        LeaveBalanceResponseDTO dto2 = new LeaveBalanceResponseDTO("lb-2", LeaveType.SICK, 5);
        List<LeaveBalanceResponseDTO> dtoList = Arrays.asList(dto1, dto2);

        when(leaveBalanceRepository.findLeaveBalanceViewByEmployeeId(employeeUuid)).thenReturn(Optional.of(dtoList));

        // execute
        List<LeaveBalanceResponseDTO> result = leaveBalanceService.getLeaveBalancesByEmployeeId(employeeIdStr);

        // verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lb-1", result.get(0).balanceId());
        assertEquals(LeaveType.SICK, result.get(1).leaveType());
        verify(leaveBalanceRepository).findLeaveBalanceViewByEmployeeId(employeeUuid);
    }

    @Test
    void getLeaveBalancesByEmployeeId_throwsNotFound_whenEmpty() {
        when(leaveBalanceRepository.findLeaveBalanceViewByEmployeeId(employeeUuid)).thenReturn(Optional.empty());

        assertThrows(LeaveBalanceNotFoundException.class, () -> leaveBalanceService.getLeaveBalancesByEmployeeId(employeeIdStr));

        verify(leaveBalanceRepository).findLeaveBalanceViewByEmployeeId(employeeUuid);
    }

    @Test
    void createLeaveBalance_createsAndReturnsDTO_whenValid() {
        // prepare employee present
        Employee employee = new Employee();
        employee.setId(employeeUuid);

        when(employeeRepository.findById(employeeUuid)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveType(employeeUuid, LeaveType.VACATION)).thenReturn(false);

        LeaveBalance saved = new LeaveBalance();
        saved.setId(UUID.randomUUID());
        saved.setBalanceId("lb-xyz");
        saved.setEmployee(employee);
        saved.setLeaveType(LeaveType.VACATION);
        saved.setLeaveBalance(15);

        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(saved);

        // execute
        LeaveBalanceDTO result = leaveBalanceService.createLeaveBalance(employeeIdStr, LeaveType.VACATION, 15);

        // verify
        assertNotNull(result);
        assertEquals(saved.getId(), result.id());
        assertEquals("lb-xyz", result.balanceId());
        assertEquals(LeaveType.VACATION, result.leaveType());
        assertEquals(15, result.leaveBalance());

        verify(employeeRepository).findById(employeeUuid);
        verify(leaveBalanceRepository).existsByEmployeeIdAndLeaveType(employeeUuid, LeaveType.VACATION);
        verify(leaveBalanceRepository).save(leaveBalanceCaptor.capture());

        LeaveBalance captured = leaveBalanceCaptor.getValue();
        assertThat(captured.getLeaveBalance()).isEqualTo(15);
        assertThat(captured.getLeaveType()).isEqualTo(LeaveType.VACATION);
        assertThat(captured.getEmployee()).isEqualTo(employee);
    }

    @Test
    void createLeaveBalance_throwsDuplicate_whenAlreadyExists() {
        Employee employee = new Employee();
        employee.setId(employeeUuid);

        when(employeeRepository.findById(employeeUuid)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveType(employeeUuid, LeaveType.SICK)).thenReturn(true);

        assertThrows(DuplicateLeaveBalanceFound.class, () -> leaveBalanceService.createLeaveBalance(employeeIdStr, LeaveType.SICK, 5));

        verify(employeeRepository).findById(employeeUuid);
        verify(leaveBalanceRepository).existsByEmployeeIdAndLeaveType(employeeUuid, LeaveType.SICK);
        verify(leaveBalanceRepository, never()).save(any());
    }

    @Test
    void createLeaveBalance_throwsEmployeeNotFound_whenEmployeeMissing() {
        when(employeeRepository.findById(employeeUuid)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> leaveBalanceService.createLeaveBalance(employeeIdStr, LeaveType.VACATION, 3));

        verify(employeeRepository).findById(employeeUuid);
        verify(leaveBalanceRepository, never()).existsByEmployeeIdAndLeaveType(any(), any());
        verify(leaveBalanceRepository, never()).save(any());
    }
}