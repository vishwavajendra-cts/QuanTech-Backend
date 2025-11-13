package com.QuanTech.QuanTech.entity;

import com.QuanTech.QuanTech.constants.enums.Gender;
import com.QuanTech.QuanTech.constants.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Employee extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "display_employee_id", length = 20, unique = true, nullable = false)
    private String displayEmployeeId;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = true)
    private String lastName;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // for login credentials
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private LoginCredential loginCredential;

    // teamManager
    @OneToOne(mappedBy = "teamManager", cascade = CascadeType.ALL, orphanRemoval = true)
    private Team teamManager;

    // lists
    // employees working under manager
    @OneToMany(mappedBy = "manager", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Employee> reportingEmployees;

    // attendance mapping
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveRequest> leaveRequests;

    // leave balance mapping
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveBalance> leaveBalances;

    // shifts mapping
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shift> shifts;

    // shift swap list 2

    // requester employee id mapping
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftSwapRequest> requesterEmployee;

    // requested employee id mapping
    @OneToMany(mappedBy = "requested", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftSwapRequest> requestedEmployee;

    // for reports
    @OneToMany(mappedBy = "generatedByManager", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsGenerated;

    @OneToMany(mappedBy = "targetEmployee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsTargeted;
}
