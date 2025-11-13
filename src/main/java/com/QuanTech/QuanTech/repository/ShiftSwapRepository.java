package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.entity.ShiftSwapRequest;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftSwapRepository extends JpaRepository<ShiftSwapRequest, UUID> {
    @Query("""
           select new com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO(
                 ssr.id as id,
                 ssr.publicId as shiftSwapId,
                 concat(rr.firstName, coalesce(concat(' ', rr.lastName), '')),
                 concat(rd.firstName, coalesce(concat(' ', rd.lastName), '')),
                 ssr.status,
                 CAST(os.shiftType AS string),
                 os.shiftDate,
                 os.shiftStartTime,
                 os.shiftEndTime,
                 os.shiftLocation,
                 CAST(rs.shiftType AS string),
                 rs.shiftDate,
                 rs.shiftStartTime,
                 rs.shiftEndTime,
                 rs.shiftLocation,
                 ssr.reason,
                 case
                     when ab.id is not null then concat(ab.firstName, coalesce(concat(' ', ab.lastName), ''))
                     else null
                 end,
                 ssr.approvedDate
           )
           from ShiftSwapRequest ssr
               join ssr.requester rr
               join ssr.requested rd
               join ssr.offeringShift os
               join ssr.requestingShift rs
               left join ssr.approvedBy ab
           where
                rr.team.teamManager.id = :managerId or rd.team.teamManager.id = :managerId
           order by ssr.createdAt desc
           """)
    List<ShiftSwapQueryResponseDTO> findShiftSwapRequestsOfTeamByManagerId(@Valid @Param("managerId") UUID managerId);

    @Query("""
            select new com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO(
                 ssr.id as id,
                 ssr.publicId as shiftSwapId,
                 concat(rr.firstName, coalesce(concat(' ', rr.lastName), '')),
                 concat(rd.firstName, coalesce(concat(' ', rd.lastName), '')),
                 ssr.status,
                 CAST(os.shiftType AS string),
                 os.shiftDate,
                 os.shiftStartTime,
                 os.shiftEndTime,
                 os.shiftLocation,
                 CAST(rs.shiftType AS string),
                 rs.shiftDate,
                 rs.shiftStartTime,
                 rs.shiftEndTime,
                 rs.shiftLocation,
                 ssr.reason,
                 case
                     when ab.id is not null then concat(ab.firstName, coalesce(concat(' ', ab.lastName), ''))
                     else null
                 end,
                 ssr.approvedDate
            )
            from ShiftSwapRequest ssr
                join ssr.requester rr
                join ssr.requested rd
                join ssr.offeringShift os
                join ssr.requestingShift rs
                left join ssr.approvedBy ab
            where
                 rr.id = :employeeId or rd.id = :employeeId
            order by ssr.createdAt desc
            """)
    List<ShiftSwapQueryResponseDTO> findSwapResponsesByEmployeeId(@Valid @Param("employeeId") UUID employeeId);
}
