package com.example.securecustomerapi.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenDTO {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    // Constructors
    public RefreshTokenDTO() {
    }
    
    public RefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
