package com.example.securecustomerapi.service;

import java.util.List;

import com.example.securecustomerapi.dto.ChangePasswordDTO;
import com.example.securecustomerapi.dto.LoginRequestDTO;
import com.example.securecustomerapi.dto.LoginResponseDTO;
import com.example.securecustomerapi.dto.RegisterRequestDTO;
import com.example.securecustomerapi.dto.ResetPasswordDTO;
import com.example.securecustomerapi.dto.UpdateProfileDTO;
import com.example.securecustomerapi.dto.UpdateRoleDTO;
import com.example.securecustomerapi.dto.UserResponseDTO;
import com.example.securecustomerapi.entity.RefreshToken;

public interface UserService {
    
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    UserResponseDTO register(RegisterRequestDTO registerRequest);
    
    UserResponseDTO getCurrentUser(String username);
    
    void changePassword(String username, ChangePasswordDTO changePasswordDTO);
    
    String generatePasswordResetToken(String email);
    
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
    
    // Exercise 7
    UserResponseDTO updateProfile(String username, UpdateProfileDTO updateProfileDTO);
    
    void deleteAccount(String username, String password);
    
    // Exercise 8
    List<UserResponseDTO> getAllUsers();
    
    UserResponseDTO updateUserRole(Long userId, UpdateRoleDTO updateRoleDTO);
    
    UserResponseDTO toggleUserStatus(Long userId);
    
    // Exercise 9
    RefreshToken createRefreshToken(String username);
    
    LoginResponseDTO refreshAccessToken(String refreshToken);
}
