package com.QuanTech.QuanTech.config;

import com.QuanTech.QuanTech.entity.LoginCredential;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserInfoDetails implements UserDetails {

    private final String username;
    private final String password;
    private final UUID employeeUuid;
    private final List<GrantedAuthority> authorities;

    public UserInfoDetails(LoginCredential loginCredential) {
        username = loginCredential.getEmail();
        password = loginCredential.getPasswordHash();
        employeeUuid = loginCredential.getEmployee().getId();
        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + loginCredential.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getEmployeeUuid() {
        return employeeUuid;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}

