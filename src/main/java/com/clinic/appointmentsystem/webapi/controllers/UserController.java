package com.clinic.appointmentsystem.webapi.controllers;

import com.clinic.appointmentsystem.application.dto.user.UpdateUserRequest;
import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.application.services.AuthService;
import com.clinic.appointmentsystem.application.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final AuthService authService;

    @GetMapping("/doctors")
    public List<UserDto> getDoctors() {
        return service.findAllDoctors();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return authService.currentUser(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication auth
    ) {
        UserDetails principal = (UserDetails) auth.getPrincipal();
        UserDto currentUser = authService.currentUserByEmail(principal.getUsername());

        if (!currentUser.id().equals(id.toString()))
            throw new IllegalArgumentException("You can only update your own profile");

        return service.updateUser(id, request);
    }
} 