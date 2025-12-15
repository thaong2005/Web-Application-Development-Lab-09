package com.example.securecustomerapi.dto;

public class LoginResponseDTO {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;
    private String refreshToken;
    
    public LoginResponseDTO() {
    }
    
    public LoginResponseDTO(String token, String username, String email, String role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    public LoginResponseDTO(String token, String username, String email, String role, String refreshToken) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
