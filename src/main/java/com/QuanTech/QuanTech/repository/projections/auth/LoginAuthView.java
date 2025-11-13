package com.QuanTech.QuanTech.repository.projections.auth;

import com.QuanTech.QuanTech.constants.enums.Role;

public interface LoginAuthView {
    String getEmail();

    Role getRole();

    String getDisplayEmployeeId();
}
