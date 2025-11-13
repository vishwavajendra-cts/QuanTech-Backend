package com.QuanTech.QuanTech.config;

import com.QuanTech.QuanTech.constants.enums.Gender;
import com.QuanTech.QuanTech.constants.enums.Role;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LoginCredential;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.LoginRepository;
import com.QuanTech.QuanTech.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Initializer to populate the database with predefined test data.
 * Creates 1 manager and 2 employees under the same team.
 *
 * Uses @Transactional to ensure all entities are properly managed within
 * the same persistence context, avoiding detached entity issues.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (employeeRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("Initializing database with predefined data...");

        // Create Manager
        Employee manager = new Employee();
        manager.setDisplayEmployeeId("EMP-001");
        manager.setFirstName("Vishwa");
        manager.setLastName("Vajendra");
        manager.setEmail("vishwa.vajendra@gmail.com");
        manager.setGender(Gender.MALE);
        manager.setPhoneNumber("1234567890");
        manager.setJobTitle("Team Manager");
        manager.setActive(true);
        manager.setDepartmentName("Engineering");
        manager.setRole(Role.MANAGER);
        manager.setManager(null); // Manager has no manager

        manager = employeeRepository.save(manager);
        log.info("Created manager: {} {}", manager.getFirstName(), manager.getLastName());

        // Create Login Credentials for Manager
        LoginCredential managerLogin = new LoginCredential();
        managerLogin.setLoginCredentialId("LOGIN-001");
        managerLogin.setEmployee(manager);
        managerLogin.setEmail(manager.getEmail());
        managerLogin.setPasswordHash(passwordEncoder.encode("manager123")); // Default password
        managerLogin.setRole(Role.MANAGER);
        loginRepository.save(managerLogin);
        log.info("Created login credentials for manager with email: {} and password: manager123", manager.getEmail());

        // Create Team with manager
        Team team = new Team();
        team.setTeamId("TEAM-001");
        team.setTeamName("Development Team");
        team.setTeamManager(manager);
        team = teamRepository.save(team);
        log.info("Created team: {}", team.getTeamName());

        // Update manager with team reference
        manager.setTeam(team);
        manager = employeeRepository.save(manager);
        log.info("Updated manager with team reference");

        // Create Employee 1
        Employee employee1 = new Employee();
        employee1.setDisplayEmployeeId("EMP-002");
        employee1.setFirstName("mohana");
        employee1.setLastName("rangam");
        employee1.setEmail("mohanarangam@gmail.com");
        employee1.setGender(Gender.MALE);
        employee1.setPhoneNumber("2345678901");
        employee1.setJobTitle("Senior Developer");
        employee1.setActive(true);
        employee1.setDepartmentName("Engineering");
        employee1.setRole(Role.EMPLOYEE);
        employee1.setManager(manager);
        employee1.setTeam(team);

        employee1 = employeeRepository.save(employee1);
        log.info("Created employee: {} {}", employee1.getFirstName(), employee1.getLastName());

        // Create Login Credentials for Employee 1
        LoginCredential employee1Login = new LoginCredential();
        employee1Login.setLoginCredentialId("LOGIN-002");
        employee1Login.setEmployee(employee1);
        employee1Login.setEmail(employee1.getEmail());
        employee1Login.setPasswordHash(passwordEncoder.encode("employee123")); // Default password
        employee1Login.setRole(Role.EMPLOYEE);
        loginRepository.save(employee1Login);
        log.info("Created login credentials for employee with email: {} and password: employee123", employee1.getEmail());

        // Create Employee 2
        Employee employee2 = new Employee();
        employee2.setDisplayEmployeeId("EMP-003");
        employee2.setFirstName("dhanush");
        employee2.setLastName("d");
        employee2.setEmail("dhanush@gmail.com");
        employee2.setGender(Gender.MALE);
        employee2.setPhoneNumber("3456789012");
        employee2.setJobTitle("QA Engineer");
        employee2.setActive(true);
        employee2.setDepartmentName("Engineering");
        employee2.setRole(Role.EMPLOYEE);
        employee2.setManager(manager);
        employee2.setTeam(team);

        employee2 = employeeRepository.save(employee2);
        log.info("Created employee: {} {}", employee2.getFirstName(), employee2.getLastName());

        // Create Login Credentials for Employee 2
        LoginCredential employee2Login = new LoginCredential();
        employee2Login.setLoginCredentialId("LOGIN-003");
        employee2Login.setEmployee(employee2);
        employee2Login.setEmail(employee2.getEmail());
        employee2Login.setPasswordHash(passwordEncoder.encode("employee123")); // Default password
        employee2Login.setRole(Role.EMPLOYEE);
        loginRepository.save(employee2Login);
        log.info("Created login credentials for employee with email: {} and password: employee123", employee2.getEmail());

        log.info("=".repeat(80));
        log.info("Database initialization completed successfully!");
        log.info("=".repeat(80));
        log.info("Team Structure:");
        log.info("  Team: {} ({})", team.getTeamName(), team.getTeamId());
        log.info("  Manager: {} {} ({})", manager.getFirstName(), manager.getLastName(), manager.getDisplayEmployeeId());
        log.info("  Team Members:");
        log.info("    - {} {} ({}) - {}", employee1.getFirstName(), employee1.getLastName(), employee1.getDisplayEmployeeId(), employee1.getJobTitle());
        log.info("    - {} {} ({}) - {}", employee2.getFirstName(), employee2.getLastName(), employee2.getDisplayEmployeeId(), employee2.getJobTitle());
        log.info("=".repeat(80));
        log.info(" All Login Team Created");

    }
}

