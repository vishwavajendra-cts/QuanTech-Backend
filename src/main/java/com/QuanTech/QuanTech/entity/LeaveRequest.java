package com.QuanTech.QuanTech.entity;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_requests")
@Data
@EntityListeners(AuditingEntityListener.class)
public class LeaveRequest extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "leave_request_id", length = 20, nullable = false, unique = true)
    private String leaveRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatus leaveStatus = LeaveStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private OffsetDateTime requestDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (endDate != null && startDate != null && endDate.isBefore((startDate))) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
