package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamShiftTableRowDTO;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.repository.projections.shift.EmployeeShiftView;
import com.QuanTech.QuanTech.repository.projections.shift.ShiftView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    @Query("""
            SELECT new com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO(
                s.id as id,
                s.publicId as shiftId,
                s.shiftDate as shiftDate,
                s.shiftStartTime as shiftStartTime,
                s.shiftEndTime as shiftEndTime,
                s.shiftStatus as shiftStatus,
                s.shiftType as shiftType,
                s.shiftLocation as shiftLocation
            )
            FROM Shift s JOIN s.employee e
                WHERE e.id = :empID
                ORDER BY s.shiftDate, s.shiftStartTime
            """)
    Optional<List<ShiftResponseDTO>> findShiftViewByEmployeeId(@Param("empID") UUID employeeId);


    @Query("""
            SELECT
                s.id as id,
                s.publicId as shiftId,
                e.id as employeeId,
                s.shiftDate as shiftDate,
                s.shiftStartTime as shiftStartTime,
                s.shiftEndTime as shiftEndTime,
                s.shiftStatus as shiftStatus,
                s.shiftType as shiftType,
                s.shiftLocation as shiftLocation
            FROM Shift s JOIN s.employee e
                 WHERE e.id in :employeeIds
            ORDER BY s.shiftDate, s.shiftStartTime
            """)
    List<ShiftView> findMultipleShiftViewByEmployeeId(@Param("employeeIds") List<UUID> employeeIds);


    @Query("""
            select new com.QuanTech.QuanTech.dto.shift.TeamShiftTableRowDTO(
                 s.id as id,
                 s.publicId as shiftId,
                 concat(e.firstName, coalesce(concat(' ', e.lastName), '')) as employeeName,
                 s.shiftDate as shiftDate,
                 s.shiftStartTime as shiftStartTime,
                 s.shiftEndTime as shiftEndTime,
                 s.shiftType as shiftType,
                 s.shiftLocation as shiftLocation,
                 s.shiftStatus as shiftStatus
            )
            from
                 Shift s join s.employee e
            where
                 e.id in :employeeIds and s.shiftDate between :start and :end
            order by e.firstName, e.lastName, s.shiftStartTime
            """)
    List<TeamShiftTableRowDTO> findTeamShiftRowByEmployeeIdsAndDate(@Param("employeeIds") List<UUID> employeeIds, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
           select new com.QuanTech.QuanTech.dto.shift.ShiftResponseDTO(
                s.id as id,
                s.publicId as shiftId,
                s.shiftDate as shiftDate,
                s.shiftStartTime as shiftStartTime,
                s.shiftEndTime as shiftEndTime,
                s.shiftStatus as shiftStatus,
                s.shiftType as shiftType,
                s.shiftLocation as shiftLocation
           )
           from
                Shift s JOIN s.employee e
           where
                e.id = :empID and s.shiftStartTime >= :now
           order by
                s.shiftDate, s.shiftStartTime
           """)
    Optional<List<ShiftResponseDTO>> findUpcomingShiftViewByEmployeeId(@Param("empID") UUID employeeId, @Param("now") OffsetDateTime now);


    @Query("""
           select
                s.id as id,
                s.publicId as shiftId,
                e.id as employeeId,
                s.shiftDate as shiftDate,
                s.shiftStartTime as shiftStartTime,
                s.shiftEndTime as shiftEndTime,
                s.shiftStatus as shiftStatus,
                s.shiftType as shiftType,
                s.shiftLocation as shiftLocation
           from
                Shift s JOIN s.employee e
           where
                e.id in :empIds and s.shiftStartTime >= :now
           order by s.shiftDate, s.shiftStartTime
           """)
    List<EmployeeShiftView> findUpcomingShiftViewByEmployeeIds(@Param("empIds") List<UUID> employeeIds, @Param("now") OffsetDateTime now);
}
