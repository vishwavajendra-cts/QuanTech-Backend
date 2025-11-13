package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.dto.login.*;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.LoginConstants;
import com.QuanTech.QuanTech.constants.enums.Role;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.LoginCredential;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.LoginFailedException;
import com.QuanTech.QuanTech.exception.custom.PasswordDoNotMatchException;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.LoginRepository;
import com.QuanTech.QuanTech.repository.projections.auth.LoginByEmailView;
import com.QuanTech.QuanTech.services.auth.JwtService;
import com.QuanTech.QuanTech.services.interfaces.LoginService;
import com.QuanTech.QuanTech.util.NanoIdGenerator;
import com.QuanTech.QuanTech.util.mappers.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class LoginServiceImpl implements LoginService {
    private final LoginRepository loginRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginServiceImpl(LoginRepository loginRepository, EmployeeRepository employeeRepository, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.employeeRepository = employeeRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public LoginResponseDTO loginEmployee(LoginDTO loginDTO) {
        return doLogin(loginDTO, Role.EMPLOYEE, LoginConstants.EMPLOYEE_LOGIN_SUCCESS, ErrorConstants.EMPLOYEE_LOGIN_FAILED);
    }

    @Override
    public LoginResponseDTO loginManager(LoginDTO loginDTO) {
        return doLogin(loginDTO, Role.MANAGER, LoginConstants.MANAGER_LOGIN_SUCCESS, ErrorConstants.MANAGER_LOGIN_FAILED);
    }

    private LoginResponseDTO doLogin(LoginDTO loginDTO, Role role, String successMessage, String errorMessage) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password()
            ));
        } catch (BadCredentialsException e) {
            throw new LoginFailedException(errorMessage + e);
        }


        LoginCredential credential = loginRepository.findByEmailWithoutProjection(loginDTO.email())
                .orElseThrow(() -> new LoginFailedException(ErrorConstants.EMPLOYEE_LOGIN_FAILED));

        if(credential.getRole() != role) {
            throw new LoginFailedException(errorMessage);
        }

        String employeeID = credential.getEmployee().getDisplayEmployeeId();
        UUID employeeUUID = credential.getEmployee().getId();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", credential.getRole().name());
        claims.put("uuid", employeeUUID.toString());
        claims.put("employeeId", employeeID);

        Assert.notNull(userDetails, ErrorConstants.EMPLOYEE_LOGIN_FAILED);

        String token = jwtService.generateToken(userDetails, claims);






        return new LoginResponseDTO(employeeUUID, credential.getEmail(), credential.getRole(), successMessage, employeeID, token);
    }

    @Override
    public List<GetAllLoginCredentialsDTO> getAllLoginCredentials() {
        List<GetAllLoginCredentialsDTO> listOfLoginDetails = new ArrayList<>();
        for (LoginCredential loginCredential : loginRepository.findAll()) {
            listOfLoginDetails.add(LoginMapper.loginEntityToDto(loginCredential));
        }
        return listOfLoginDetails;
    }

    @Override
    @Transactional
    public CreateLoginCredentialResponseDTO createLoginCredentials(CreateLoginCredentialDTO createLoginCredentialDTO) {
        Employee employee = employeeRepository.findByEmployeeId(createLoginCredentialDTO.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND));

        LoginCredential loginCredential = new LoginCredential();

        int loginCredentialIdLength = 10;
        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                loginCredentialIdLength
        );

        loginCredential.setLoginCredentialId(nanoId);
        loginCredential.setEmail(createLoginCredentialDTO.email());
        loginCredential.setPasswordHash(passwordEncoder.encode(createLoginCredentialDTO.password()));
        loginCredential.setEmployee(employee);
        loginCredential.setRole(createLoginCredentialDTO.role());

        loginRepository.save(loginCredential);

        return new CreateLoginCredentialResponseDTO(loginCredential.getLoginCredentialId(), LoginConstants.LOGIN_DETAILS_CREATED, loginCredential.getEmployee().getDisplayEmployeeId());
    }

    @Override
    public GetAllLoginCredentialsDTO getLoginCredentialsByEmail(String email) {
        LoginByEmailView loginCredential = loginRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.LOGIN_CREDENTIALS_NOT_FOUND + email));

        return LoginMapper.loginViewToDto(loginCredential);
    }

    @Override
    @Transactional
    public ChangePasswordResponseDTO changePassword(String email, ChangePasswordDTO changePasswordDTO) {
        if (!changePasswordDTO.newPassword().equals(changePasswordDTO.confirmPassword())) {
            throw new PasswordDoNotMatchException(ErrorConstants.NEW_PASSWORD_CONFIRM_PASSWORD_NOT_MATCH);
        }
        LoginCredential loginCredential = loginRepository.findByEmailWithoutProjection(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.LOGIN_CREDENTIALS_NOT_FOUND + email));

        loginCredential.setPasswordHash(passwordEncoder.encode(changePasswordDTO.confirmPassword()));
        loginRepository.save(loginCredential);

        String message = LoginConstants.PASSWORD_CHANGED_SUCCESS;
        return new ChangePasswordResponseDTO(email, message);
    }
}
