package com.QuanTech.QuanTech.util.mappers;

import com.QuanTech.QuanTech.dto.login.GetAllLoginCredentialsDTO;
import com.QuanTech.QuanTech.entity.LoginCredential;
import com.QuanTech.QuanTech.repository.projections.auth.LoginByEmailView;

public class LoginMapper {
    public static GetAllLoginCredentialsDTO loginEntityToDto(LoginCredential loginCredential) {
        return new GetAllLoginCredentialsDTO(
                loginCredential.getLoginCredentialId(),
                loginCredential.getEmail(),
                loginCredential.getPasswordHash(),
                loginCredential.getEmployee().getDisplayEmployeeId(),
                loginCredential.getRole()
        );
    }

    public static GetAllLoginCredentialsDTO loginViewToDto(LoginByEmailView view) {
        if (view == null) return null;

        return new GetAllLoginCredentialsDTO(
                view.getLoginId(),
                view.getEmail(),
                view.getPasswordHash(),
                view.getDisplayEmployeeId(),
                view.getRole()
        );
    }
}
