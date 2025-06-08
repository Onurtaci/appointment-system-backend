package com.clinic.appointmentsystem.application.services;

import com.clinic.appointmentsystem.application.dto.user.UpdateUserRequest;
import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.application.mapper.UserMapper;
import com.clinic.appointmentsystem.domain.entities.User;
import com.clinic.appointmentsystem.domain.enums.Role;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> findAllDoctors() {
        return userRepository.findByRole(Role.DOCTOR)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public UserDto updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.getEmail().equals(request.email())) {
            userRepository.findByEmail(request.email())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new IllegalArgumentException("Email is already in use");
                        }
                    });
        }

        try {
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmail(request.email());
            user.setUpdatedAt(LocalDateTime.now());

            User updatedUser = userRepository.save(user);
            return userMapper.toDto(updatedUser);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to update user: " + e.getMessage());
        }
    }
}