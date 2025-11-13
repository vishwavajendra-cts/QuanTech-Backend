package com.QuanTech.QuanTech.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "reports")
@EntityListeners(AuditingEntityListener.class)
public class Report extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "report_id", length = 20, nullable = false, unique = true)
    private String reportId;

    @Column(name = "report_name", nullable = false, length = 150)
    private String reportName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_manager_id", nullable = false)
    private Employee generatedByManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_team_id")
    private Team targetTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_employee_id")
    private Employee targetEmployee;

    @Column(name = "report_start_date", nullable = false)
    private OffsetDateTime reportStartDate;

    @Column(name = "report_end_date", nullable = false)
    private OffsetDateTime reportEndDate;

    @Column(name = "total_days_present")
    private Integer totalDaysPresent;

    @Column(name = "total_days_absent")
    private Integer totalDaysAbsent;

    @Column(name = "total_hours_worked")
    private double totalHoursWorked;

    @CreatedDate
    @Column(name = "generated_at", updatable = false)
    private Instant generatedAt;
}
