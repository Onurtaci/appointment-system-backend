package com.clinic.appointmentsystem.webapi.controllers;

import com.clinic.appointmentsystem.application.dto.auth.AuthResponse;
import com.clinic.appointmentsystem.application.dto.auth.LoginRequest;
import com.clinic.appointmentsystem.application.dto.auth.RegisterRequest;
import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.application.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return service.register(r);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        UserDetails principal = (UserDetails) auth.getPrincipal();
        return service.currentUserByEmail(principal.getUsername());
    }
}
