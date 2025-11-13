package com.QuanTech.QuanTech.repository.projections.auth;

import com.QuanTech.QuanTech.constants.enums.Role;

public interface LoginByEmailView {
    String getLoginId();

    String getEmail();

    String getPasswordHash();

    Role getRole();

    String getDisplayEmployeeId();
}
