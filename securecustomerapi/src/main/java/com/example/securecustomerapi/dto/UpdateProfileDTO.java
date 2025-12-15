package com.example.securecustomerapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileDTO {
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    // Constructors
    public UpdateProfileDTO() {
    }
    
    public UpdateProfileDTO(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
