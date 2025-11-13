package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.dto.leaveRequests.*;
import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LeaveBalance;
import com.QuanTech.QuanTech.entity.LeaveRequest;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.InvalidLeaveRequestException;
import com.QuanTech.QuanTech.exception.custom.LeaveRequestNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.LeaveBalanceRepository;
import com.QuanTech.QuanTech.repository.LeaveRequestRepository;
import com.QuanTech.QuanTech.services.LeaveRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private UUID employeeId;
    private String employeeIdStr;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        employeeIdStr = employeeId.toString();
    }

    @Test
    void createLeaveRequest_success() {
        Employee emp = new Employee();
        emp.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(emp));

        LeaveRequestCreateRequestDTO req = new LeaveRequestCreateRequestDTO(
                LeaveType.VACATION,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12),
                "Reason is sufficiently long"
        );

        LeaveRequest saved = new LeaveRequest();
        saved.setId(UUID.randomUUID());
        saved.setLeaveRequestId("LR-123");
        saved.setEmployee(emp);
        saved.setLeaveType(req.leaveType());
        saved.setStartDate(req.startDate());
        saved.setEndDate(req.endDate());
        saved.setLeaveStatus(LeaveStatus.PENDING);
        saved.setRequestDate(OffsetDateTime.now());
        saved.setReason(req.reason());

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(saved);

        LeaveRequestResponseDTO result = leaveRequestService.createLeaveRequest(employeeIdStr, req);

        assertNotNull(result);
        assertEquals("LR-123", result.leaveRequestId());
        assertEquals(LeaveType.VACATION, result.leaveType());
        assertEquals(3L, result.days());
        verify(employeeRepository).findById(employeeId);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void createLeaveRequest_employeeNotFound_throws() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        LeaveRequestCreateRequestDTO req = new LeaveRequestCreateRequestDTO(
                LeaveType.SICK,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                "Valid reason text"
        );

        assertThrows(EmployeeNotFoundException.class, () -> leaveRequestService.createLeaveRequest(employeeIdStr, req));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void createLeaveRequest_invalidDates_throws() {
        Employee emp = new Employee();
        emp.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(emp));

        LeaveRequestCreateRequestDTO req = new LeaveRequestCreateRequestDTO(
                LeaveType.VACATION,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 5),
                "Valid reason text"
        );

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.createLeaveRequest(employeeIdStr, req));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void getEmployeeLeaveRequests_returnsList() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(UUID.randomUUID());
        lr.setLeaveRequestId("LR-1");
        lr.setStartDate(LocalDate.of(2025, 1, 1));
        lr.setEndDate(LocalDate.of(2025, 1, 1));
        lr.setLeaveStatus(LeaveStatus.APPROVED);
        lr.setRequestDate(OffsetDateTime.now());

        when(leaveRequestRepository.findLeaveRequestsByEmployeeId(employeeId)).thenReturn(List.of(lr));

        List<LeaveRequestResponseDTO> list = leaveRequestService.getEmployeeLeaveRequests(employeeIdStr);

        assertThat(list).hasSize(1);
        assertEquals("LR-1", list.get(0).leaveRequestId());
        verify(leaveRequestRepository).findLeaveRequestsByEmployeeId(employeeId);
    }

    @Test
    void getTeamLeaveRequests_returnsMappedList() {
        UUID managerId = UUID.randomUUID();
        LeaveRequest lr = new LeaveRequest();
        lr.setId(UUID.randomUUID());
        lr.setLeaveRequestId("LR-T1");
        Employee e = new Employee();
        e.setId(UUID.randomUUID());
        e.setFirstName("Alice");
        e.setLastName("Smith");
        lr.setEmployee(e);
        lr.setLeaveType(LeaveType.VACATION);
        lr.setStartDate(LocalDate.of(2025, 2, 1));
        lr.setEndDate(LocalDate.of(2025, 2, 2));
        when(leaveRequestRepository.findTeamLeaveRequests(managerId)).thenReturn(List.of(lr));

        var result = leaveRequestService.getTeamLeaveRequests(managerId.toString());

        assertThat(result).isNotEmpty();
        assertEquals(lr.getId(), result.get(0).requestId());
        verify(leaveRequestRepository).findTeamLeaveRequests(managerId);
    }

    @Test
    void actionOnLeaveRequest_approve_success() {
        UUID leaveRequestId = UUID.randomUUID();
        Employee emp = new Employee();
        emp.setId(employeeId);

        LeaveRequest lr = new LeaveRequest();
        lr.setId(leaveRequestId);
        lr.setLeaveRequestId("LR-A1");
        lr.setEmployee(emp);
        lr.setLeaveType(LeaveType.VACATION);
        lr.setStartDate(LocalDate.of(2025, 3, 1));
        lr.setEndDate(LocalDate.of(2025, 3, 2)); // 2 days inclusive
        lr.setLeaveStatus(LeaveStatus.PENDING);

        LeaveBalance lb = new LeaveBalance();
        lb.setId(UUID.randomUUID());
        lb.setEmployee(emp);
        lb.setLeaveType(LeaveType.VACATION);
        lb.setLeaveBalance(5);

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(lr));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveType(emp.getId(), lr.getLeaveType())).thenReturn(Optional.of(lb));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveRequestService.actionOnLeaveRequest(UUID.randomUUID().toString(), leaveRequestId.toString(), new LeaveRequestActionDTO(LeaveStatus.APPROVED));

        assertEquals(3, lb.getLeaveBalance()); // 5 - 2 = 3
        assertEquals(LeaveStatus.APPROVED, lr.getLeaveStatus());
        verify(leaveBalanceRepository).save(lb);
        verify(leaveRequestRepository).save(lr);
    }

    @Test
    void actionOnLeaveRequest_approve_insufficient_throws() {
        UUID leaveRequestId = UUID.randomUUID();
        Employee emp = new Employee();
        emp.setId(employeeId);

        LeaveRequest lr = new LeaveRequest();
        lr.setId(leaveRequestId);
        lr.setEmployee(emp);
        lr.setLeaveType(LeaveType.VACATION);
        lr.setStartDate(LocalDate.of(2025, 4, 1));
        lr.setEndDate(LocalDate.of(2025, 4, 3)); // 3 days
        lr.setLeaveStatus(LeaveStatus.PENDING);

        LeaveBalance lb = new LeaveBalance();
        lb.setEmployee(emp);
        lb.setLeaveType(LeaveType.VACATION);
        lb.setLeaveBalance(2);

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(lr));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveType(emp.getId(), lr.getLeaveType())).thenReturn(Optional.of(lb));

        assertThrows(IllegalStateException.class, () ->
                leaveRequestService.actionOnLeaveRequest(UUID.randomUUID().toString(), leaveRequestId.toString(), new LeaveRequestActionDTO(LeaveStatus.APPROVED))
        );

        verify(leaveBalanceRepository, never()).save(any());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void actionOnLeaveRequest_notFound_throws() {
        UUID leaveRequestId = UUID.randomUUID();
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.empty());

        assertThrows(LeaveRequestNotFoundException.class, () ->
                leaveRequestService.actionOnLeaveRequest(UUID.randomUUID().toString(), leaveRequestId.toString(), new LeaveRequestActionDTO(LeaveStatus.APPROVED))
        );
    }

    @Test
    void actionOnLeaveRequest_alreadyProcessed_throws() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest lr = new LeaveRequest();
        lr.setId(leaveRequestId);
        lr.setLeaveStatus(LeaveStatus.APPROVED); // already processed

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(lr));

        assertThrows(IllegalStateException.class, () ->
                leaveRequestService.actionOnLeaveRequest(UUID.randomUUID().toString(), leaveRequestId.toString(), new LeaveRequestActionDTO(LeaveStatus.REJECTED))
        );

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void getLeaveRequestsStatsByManager_returnsCounts() {
        UUID managerId = UUID.randomUUID();
        when(leaveRequestRepository.countByManagerAndStatus(managerId, LeaveStatus.PENDING)).thenReturn(2L);
        when(leaveRequestRepository.countByManagerAndStatus(managerId, LeaveStatus.APPROVED)).thenReturn(5L);
        when(leaveRequestRepository.countByManagerAndStatus(managerId, LeaveStatus.REJECTED)).thenReturn(1L);
        when(leaveRequestRepository.countByOnLeaveToday(managerId, LocalDate.now())).thenReturn(1L);

        var dto = leaveRequestService.getLeaveRequestsStatsByManager(managerId.toString());

        assertNotNull(dto);
        assertEquals(2L, dto.pending());
        assertEquals(5L, dto.approved());
        assertEquals(1L, dto.rejected());
        assertEquals(1L, dto.onLeaveToday());
    }

    @Test
    void getLeaveRequestManagerDashboard_returnsList() {
        UUID managerId = UUID.randomUUID();
        LeaveRequest lr = new LeaveRequest();
        lr.setId(UUID.randomUUID());
        lr.setLeaveRequestId("LR-MDB-1");
        Employee e = new Employee();
        e.setId(UUID.randomUUID());
        e.setFirstName("Bob");
        e.setLastName("Jones");
        lr.setEmployee(e);
        lr.setLeaveType(LeaveType.SICK);
        lr.setStartDate(LocalDate.of(2025, 5, 1));
        lr.setEndDate(LocalDate.of(2025, 5, 1));

        when(leaveRequestRepository.leaveRequestManagerDashboard(managerId)).thenReturn(List.of(
                new ManagerLeaveRequestDashboardResponseDTO(
                        lr.getLeaveRequestId(),
                        "Bob Jones",
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate()
                )
        ));

        var result = leaveRequestService.getLeaveRequestManagerDashboard(managerId.toString());
        assertThat(result).hasSize(1);
        assertEquals("LR-MDB-1", result.get(0).leaveRequestId());
    }

    @Test
    void getLeaveRequestEmployeeDashboard_returnsList() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(UUID.randomUUID());
        lr.setLeaveRequestId("LR-ED-1");
        lr.setLeaveType(LeaveType.SICK);
        lr.setStartDate(LocalDate.of(2025, 6, 1));
        lr.setEndDate(LocalDate.of(2025, 6, 2));

        when(leaveRequestRepository.leaveRequestEmployeeDashboard(employeeId)).thenReturn(List.of(
                new EmployeeLeaveRequestDashboardResponseDTO(
                        lr.getLeaveRequestId(),
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        LeaveStatus.PENDING
                )
        ));

        var result = leaveRequestService.getLeaveRequestEmployeeDashboard(employeeIdStr);
        assertThat(result).hasSize(1);
        assertEquals("LR-ED-1", result.get(0).leaveRequestId());
    }
}