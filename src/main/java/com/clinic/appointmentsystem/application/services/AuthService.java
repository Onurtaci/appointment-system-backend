package com.clinic.appointmentsystem.application.services;

import com.clinic.appointmentsystem.application.dto.auth.AuthResponse;
import com.clinic.appointmentsystem.application.dto.auth.LoginRequest;
import com.clinic.appointmentsystem.application.dto.auth.RegisterRequest;
import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.application.mapper.UserMapper;
import com.clinic.appointmentsystem.domain.entities.User;
import com.clinic.appointmentsystem.domain.enums.Role;
import com.clinic.appointmentsystem.infrastructure.security.JwtService;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final UserMapper mapper;

    public AuthResponse register(RegisterRequest r) {
        if (repo.findByEmail(r.email()).isPresent()) throw new IllegalArgumentException("ACCOUNT_EXISTS");

        var user = User.builder()
                .id(UUID.randomUUID())
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email())
                .passwordHash(encoder.encode(r.password()))
                .role(Role.valueOf(r.role()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repo.save(user);

        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(mapper.toDto(user), token);
    }

    public AuthResponse login(LoginRequest req) {
        var user = repo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("INVALID_CREDENTIALS");

        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(mapper.toDto(user), token);
    }

    public UserDto currentUserByEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return mapper.toDto(user);
    }

    public UserDto currentUser(UUID id) {
        return mapper.toDto(
                repo.findById(id).orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"))
        );
    }
}
