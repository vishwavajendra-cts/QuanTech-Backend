package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.dto.leaveRequests.EmployeeLeaveRequestDashboardResponseDTO;
import com.QuanTech.QuanTech.dto.leaveRequests.ManagerLeaveRequestDashboardResponseDTO;
import com.QuanTech.QuanTech.entity.LeaveRequest;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    @Query("""
           SELECT
                lr
           FROM
                LeaveRequest lr
           WHERE lr.employee.team.teamManager.id = :managerId
           ORDER BY
                lr.requestDate DESC
           """)
    List<LeaveRequest> findTeamLeaveRequests(@Param("managerId") UUID managerId);


    @Query("""
           select
                lr
           from LeaveRequest lr
           where
                lr.employee.id = :employeeId
           order by
                lr.requestDate desc
           """)
    List<LeaveRequest> findLeaveRequestsByEmployeeId(@Valid @Param("employeeId") UUID employeeId);

    @Query("""
           SELECT
                COUNT(lr)
           FROM
                LeaveRequest lr
                JOIN lr.employee e
                JOIN e.team t
           WHERE
                t.teamManager.id = :managerId AND lr.leaveStatus = :leaveStatus
           """)
    long countByManagerAndStatus(@Param("managerId") UUID managerId, @Param("leaveStatus") LeaveStatus leaveStatus);

    @Query("""
           SELECT
                COUNT(lr)
           FROM
                LeaveRequest lr
           WHERE
                lr.employee.team.teamManager.id = :managerId
                AND lr.leaveStatus = 'APPROVED'
                AND :today BETWEEN lr.startDate AND lr.endDate
           """)
    long countByOnLeaveToday(@Param("managerId") UUID managerId, @Param("today") LocalDate today);

    @Query("""
           select new com.QuanTech.QuanTech.dto.leaveRequests.ManagerLeaveRequestDashboardResponseDTO(
                lr.leaveRequestId,
                concat(e.firstName, coalesce(concat(' ', e.lastName), '')) as employeeName,
                lr.leaveType,
                lr.startDate,
                lr.endDate
           )
           from
                LeaveRequest lr JOIN lr.employee e JOIN e.team t JOIN t.teamManager tm
           where
                tm.id = :managerId
           """)
    List<ManagerLeaveRequestDashboardResponseDTO> leaveRequestManagerDashboard(@Param("managerId") UUID managerId);


    @Query("""
           select new com.QuanTech.QuanTech.dto.leaveRequests.EmployeeLeaveRequestDashboardResponseDTO(
                lr.leaveRequestId,
                lr.leaveType,
                lr.startDate,
                lr.endDate,
                lr.leaveStatus
           )
           from
                LeaveRequest lr JOIN lr.employee e
           where
                e.id = :employeeId
           """)
    List<EmployeeLeaveRequestDashboardResponseDTO> leaveRequestEmployeeDashboard(@Param("employeeId") UUID employeeId);
}
