package com.clinic.appointmentsystem.application.dto.auth;

public record RegisterRequest(String firstName,
                              String lastName,
                              String email,
                              String password,
                              String role) {
}
