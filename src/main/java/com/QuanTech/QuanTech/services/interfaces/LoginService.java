package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.login.*;


import java.util.List;

public interface LoginService {
    LoginResponseDTO loginEmployee(LoginDTO loginDTO);

    LoginResponseDTO loginManager(LoginDTO loginDTO);

    CreateLoginCredentialResponseDTO createLoginCredentials(CreateLoginCredentialDTO createLoginCredentialDTO);

    List<GetAllLoginCredentialsDTO> getAllLoginCredentials();

    GetAllLoginCredentialsDTO getLoginCredentialsByEmail(String email);

    ChangePasswordResponseDTO changePassword(String email, ChangePasswordDTO newPassword);
}
