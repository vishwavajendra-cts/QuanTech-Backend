package com.QuanTech.QuanTech.config;

import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.repository.LoginRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoDetailsService implements UserDetailsService {

    private final LoginRepository loginRepository;


    public UserInfoDetailsService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loginRepository.findByEmailWithoutProjection(email)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND + email));
    }
}
