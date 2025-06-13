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

/**
 * UserService - Kullanıcı yönetim servisi
 * 
 * Bu servis kullanıcı bilgilerini listeleme, güncelleme ve doktor listesi alma işlemlerini yönetir.
 * Kullanıcı profil yönetimi ve doktor arama işlevlerini sağlar.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Sistemdeki tüm doktorları listeler
     * 
     * @return Doktor listesi
     */
    @Transactional(readOnly = true)
    public List<UserDto> findAllDoctors() {
        // Sadece DOCTOR rolündeki kullanıcıları getir
        return userRepository.findByRole(Role.DOCTOR)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Kullanıcı bilgilerini günceller
     * 
     * @param userId Güncellenecek kullanıcının ID'si
     * @param request Güncelleme isteği (ad, soyad, email)
     * @return Güncellenmiş kullanıcı bilgileri
     * @throws EntityNotFoundException Kullanıcı bulunamadığında
     * @throws IllegalArgumentException Email zaten kullanımda olduğunda veya güncelleme başarısız olduğunda
     */
    @Transactional
    public UserDto updateUser(UUID userId, UpdateUserRequest request) {
        // Kullanıcının varlığını kontrol et
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Email değişikliği varsa, yeni email'in başka kullanıcı tarafından kullanılıp kullanılmadığını kontrol et
        if (!user.getEmail().equals(request.email())) {
            userRepository.findByEmail(request.email())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new IllegalArgumentException("Email is already in use");
                        }
                    });
        }

        try {
            // Kullanıcı bilgilerini güncelle
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmail(request.email());
            user.setUpdatedAt(LocalDateTime.now());

            // Güncellenmiş kullanıcıyı kaydet ve DTO'ya çevir
            User updatedUser = userRepository.save(user);
            return userMapper.toDto(updatedUser);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to update user: " + e.getMessage());
        }
    }
}