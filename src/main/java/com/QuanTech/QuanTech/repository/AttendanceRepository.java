package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.constants.enums.AttendanceStatus;
import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceRowDTO;
import com.QuanTech.QuanTech.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    @Query("""
            select new com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO(
                 a.attendanceId,
                 a.date,
                 a.checkIn,
                 a.checkOut,
                 a.hoursWorked,
                 a.attendanceStatus,
                 a.location
            )
            from Attendance a
                 join a.employee e
            where
                 e.id = :employeeId
            order by a.date desc
            """)
    List<AttendanceResponseDTO> findAllByEmployeeOrderByDateDesc(@Param("employeeId") UUID employeeId);

    @Query("""
            select
                 a from Attendance a
                 join a.employee e
            where
                 e.id = :employeeId and a.attendanceStatus = :status
            ORDER by a.date desc
            """)
    List<Attendance> findLatestByEmployeeAndStatus(@Param("employeeId") UUID employeeId, @Param("status") AttendanceStatus status);

    @Query("""
            select new com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceRowDTO(
                 e.displayEmployeeId,
                 concat(e.firstName, ' ', coalesce(e.lastName, '')),
                 a.checkIn,
                 a.checkOut,
                 a.hoursWorked,
                 a.attendanceStatus
            )
            from
                 Attendance a
                 join a.employee e
                 join e.team t
                 join t.teamManager m
            where
                 m.id = :managerId
                 and a.date between :start and :end
            order by e.displayEmployeeId asc, a.checkIn asc
            """)
    List<ManagerAttendanceRowDTO> findTeamAttendance(@Param("managerId") UUID managerId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}

