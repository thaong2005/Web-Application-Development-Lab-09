package com.example.securecustomerapi.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.securecustomerapi.dto.ChangePasswordDTO;
import com.example.securecustomerapi.dto.LoginRequestDTO;
import com.example.securecustomerapi.dto.LoginResponseDTO;
import com.example.securecustomerapi.dto.RegisterRequestDTO;
import com.example.securecustomerapi.dto.ResetPasswordDTO;
import com.example.securecustomerapi.dto.UpdateProfileDTO;
import com.example.securecustomerapi.dto.UpdateRoleDTO;
import com.example.securecustomerapi.dto.UserResponseDTO;
import com.example.securecustomerapi.entity.RefreshToken;
import com.example.securecustomerapi.entity.Role;
import com.example.securecustomerapi.entity.User;
import com.example.securecustomerapi.exception.DuplicateResourceException;
import com.example.securecustomerapi.exception.ResourceNotFoundException;
import com.example.securecustomerapi.repository.RefreshTokenRepository;
import com.example.securecustomerapi.repository.UserRepository;
import com.example.securecustomerapi.security.JwtTokenProvider;

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
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
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
        
        // Generate refresh token
        RefreshToken refreshToken = createRefreshToken(user.getUsername());
        
        return new LoginResponseDTO(
            token,
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            refreshToken.getToken()
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
    
    // EXERCISE 7: User Profile Management 
    
    @Override
    public UserResponseDTO updateProfile(String username, UpdateProfileDTO updateProfileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(updateProfileDTO.getEmail())) {
            if (userRepository.existsByEmail(updateProfileDTO.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
        }
        
        // Update profile
        user.setFullName(updateProfileDTO.getFullName());
        user.setEmail(updateProfileDTO.getEmail());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public void deleteAccount(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        
        // Soft delete - set isActive to false
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    // EXERCISE 8: Admin Endpoints 
    
    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponseDTO updateUserRole(Long userId, UpdateRoleDTO updateRoleDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setRole(updateRoleDTO.getRole());
        User updatedUser = userRepository.save(user);
        
        return convertToDTO(updatedUser);
    }
    
    @Override
    public UserResponseDTO toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Toggle isActive status
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        
        return convertToDTO(updatedUser);
    }
    
    // EXERCISE 9: Refresh Token 
    
    @Override
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Delete existing refresh token if any
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        
        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7 days expiry
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    public LoginResponseDTO refreshAccessToken(String refreshTokenStr) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
        
        // Check if expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }
        
        // Get user
        User user = refreshToken.getUser();
        
        // Generate new access token with proper authorities
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        String newAccessToken = tokenProvider.generateToken(authentication);
        
        // Optionally generate new refresh token
        RefreshToken newRefreshToken = createRefreshToken(user.getUsername());
        
        return new LoginResponseDTO(
                newAccessToken,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                newRefreshToken.getToken()
        );
    }
}
