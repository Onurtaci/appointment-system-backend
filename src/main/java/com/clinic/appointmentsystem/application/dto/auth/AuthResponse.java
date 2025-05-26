package com.clinic.appointmentsystem.application.dto.auth;

import com.clinic.appointmentsystem.application.dto.user.UserDto;

public record AuthResponse(UserDto user, String token) {
}
