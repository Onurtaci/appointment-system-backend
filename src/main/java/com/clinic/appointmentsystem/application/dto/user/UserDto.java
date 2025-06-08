package com.clinic.appointmentsystem.application.dto.user;

public record UserDto(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        String createdAt,
        String updatedAt
) {
}