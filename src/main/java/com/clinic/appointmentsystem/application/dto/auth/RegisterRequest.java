package com.clinic.appointmentsystem.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,
        
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(regexp = "^\\d+$", message = "Password must contain only numbers")
        String password,
        
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "^(PATIENT|DOCTOR)$", message = "Role must be either PATIENT or DOCTOR")
        String role
) {
}
