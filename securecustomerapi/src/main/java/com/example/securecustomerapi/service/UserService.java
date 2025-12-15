package com.example.securecustomerapi.service;

import com.example.securecustomerapi.dto.ChangePasswordDTO;
import com.example.securecustomerapi.dto.LoginRequestDTO;
import com.example.securecustomerapi.dto.LoginResponseDTO;
import com.example.securecustomerapi.dto.RegisterRequestDTO;
import com.example.securecustomerapi.dto.ResetPasswordDTO;
import com.example.securecustomerapi.dto.UserResponseDTO;

public interface UserService {
    
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    UserResponseDTO register(RegisterRequestDTO registerRequest);
    
    UserResponseDTO getCurrentUser(String username);
    
    void changePassword(String username, ChangePasswordDTO changePasswordDTO);
    
    String generatePasswordResetToken(String email);
    
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
