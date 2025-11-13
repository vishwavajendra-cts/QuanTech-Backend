package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceResponseDTO;
import com.QuanTech.QuanTech.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    boolean existsByEmployeeIdAndLeaveType(UUID employeeId, LeaveType leaveType);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveType(@Param("employeeId") UUID employeeId, @Param("leaveType") LeaveType leaveType);

    @Query("""
            select new com.QuanTech.QuanTech.dto.leaveBalance.LeaveBalanceResponseDTO(
                 lb.balanceId as balanceId,
                 lb.leaveType as leaveType,
                 lb.leaveBalance as leaveBalance
            )
            from LeaveBalance lb
                 join lb.employee e
            where
                 e.id = :employeeId
            """)
    Optional<List<LeaveBalanceResponseDTO>> findLeaveBalanceViewByEmployeeId(@Param("employeeId") UUID employeeId);
}
