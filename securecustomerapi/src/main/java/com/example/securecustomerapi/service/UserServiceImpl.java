package com.example.securecustomerapi.service;

import com.example.securecustomerapi.dto.*;
import com.example.securecustomerapi.entity.Role;
import com.example.securecustomerapi.entity.User;
import com.example.securecustomerapi.exception.DuplicateResourceException;
import com.example.securecustomerapi.exception.ResourceNotFoundException;
import com.example.securecustomerapi.repository.UserRepository;
import com.example.securecustomerapi.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        String token = tokenProvider.generateToken(authentication);
        
        // Get user details
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return new LoginResponseDTO(
            token,
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    @Override
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(Role.USER);  // Default role
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        
        return convertToDTO(savedUser);
    }
    
    @Override
    public UserResponseDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToDTO(user);
    }
    
    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getIsActive(),
            user.getCreatedAt()
        );
    }
    
    @Override
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) {
        // Get current user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Check new password matches confirm
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Check new password is different from current
        if (changePasswordDTO.getCurrentPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        // Hash and update password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public String generatePasswordResetToken(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with this email not found"));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        
        // Set token and expiry (1 hour)
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        
        // In real app, send email with reset link
        // For now, just return the token
        return resetToken;
    }
    
    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // Find user by reset token
        User user = userRepository.findByResetToken(resetPasswordDTO.getResetToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));
        
        // Check if token is expired
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        
        // Check passwords match
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        
        // Clear reset token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
    }
}
