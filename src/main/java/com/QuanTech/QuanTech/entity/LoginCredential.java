package com.QuanTech.QuanTech.entity;


import com.QuanTech.QuanTech.constants.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "login_credentials")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class LoginCredential extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "login_credential_id", length = 20, nullable = false, unique = true)
    private String loginCredentialId;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NonNull
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @NonNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.EMPLOYEE;
}
