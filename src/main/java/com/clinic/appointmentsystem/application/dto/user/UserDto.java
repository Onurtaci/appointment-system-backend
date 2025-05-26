package com.clinic.appointmentsystem.application.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
    String id,
    String email,
    String firstName,
    String lastName,
    String role,
    String createdAt,
    String updatedAt
) {
    public UserDto(UUID id, String email, String firstName, String lastName, String role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(
            id != null ? id.toString() : null,
            email,
            firstName,
            lastName,
            role,
            createdAt != null ? createdAt.toString() : null,
            updatedAt != null ? updatedAt.toString() : null
        );
    }
} 