package com.QuanTech.QuanTech.repository;

import com.QuanTech.QuanTech.entity.LoginCredential;
import com.QuanTech.QuanTech.constants.enums.Role;
import com.QuanTech.QuanTech.repository.projections.auth.LoginAuthView;
import com.QuanTech.QuanTech.repository.projections.auth.LoginByEmailView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginRepository extends JpaRepository<LoginCredential, UUID> {
    @Query("select l from LoginCredential l where l.email = :email")
    Optional<LoginCredential> findByEmailWithoutProjection(@Param("email") String email);

    @Query("""
             select
                 lc.email as email,
                 lc.role as role,
                 e.displayEmployeeId as displayEmployeeId
             from LoginCredential lc
                 join lc.employee e
             where lc.email = :email and lc.passwordHash = :passwordHash and lc.role = :role
            """)
    Optional<LoginAuthView> findAuthByEmailAndPasswordAndRole(@Param("email") String email, @Param("passwordHash") String password, @Param("role") Role role);


    @Query("""
            select
                 lc.loginCredentialId as loginId,
                 lc.email as email,
                 lc.passwordHash as passwordHash,
                 lc.role as role,
                 e.displayEmployeeId as displayEmployeeId
            from LoginCredential lc
                 join lc.employee e
            where lc.email = :email
            """)
    Optional<LoginByEmailView> findByEmail(@Param("email") String email);
}
