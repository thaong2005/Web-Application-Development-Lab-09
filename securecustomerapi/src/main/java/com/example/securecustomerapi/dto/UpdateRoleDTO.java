package com.example.securecustomerapi.dto;

import com.example.securecustomerapi.entity.Role;

import jakarta.validation.constraints.NotNull;

public class UpdateRoleDTO {
    
    @NotNull(message = "Role is required")
    private Role role;
    
    // Constructors
    public UpdateRoleDTO() {
    }
    
    public UpdateRoleDTO(Role role) {
        this.role = role;
    }
    
    // Getters and Setters
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
