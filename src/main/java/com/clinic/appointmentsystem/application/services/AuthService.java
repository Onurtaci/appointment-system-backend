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

/**
 * AuthService - Kimlik doğrulama ve yetkilendirme servisi
 * 
 * Bu servis kullanıcı kaydı, giriş işlemleri ve JWT token yönetimini gerçekleştirir.
 * Şifre hashleme, token üretimi ve kullanıcı doğrulama işlemlerini yönetir.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final UserMapper mapper;

    /**
     * Yeni kullanıcı kaydı yapar
     * 
     * @param r Kayıt isteği (ad, soyad, email, şifre, rol)
     * @return Kimlik doğrulama yanıtı (kullanıcı bilgileri ve JWT token)
     * @throws IllegalArgumentException Email zaten kullanımda olduğunda
     */
    public AuthResponse register(RegisterRequest r) {
        // Email'in zaten kullanımda olup olmadığını kontrol et
        if (repo.findByEmail(r.email()).isPresent()) throw new IllegalArgumentException("ACCOUNT_EXISTS");

        // Yeni kullanıcı nesnesi oluştur
        var user = User.builder()
                .id(UUID.randomUUID())
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email())
                .passwordHash(encoder.encode(r.password())) // Şifreyi hashle
                .role(Role.valueOf(r.role()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Kullanıcıyı veritabanına kaydet
        repo.save(user);

        // JWT token üret ve yanıt oluştur
        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(mapper.toDto(user), token);
    }

    /**
     * Kullanıcı girişi yapar
     * 
     * @param req Giriş isteği (email, şifre)
     * @return Kimlik doğrulama yanıtı (kullanıcı bilgileri ve JWT token)
     * @throws IllegalArgumentException Geçersiz kimlik bilgileri olduğunda
     */
    public AuthResponse login(LoginRequest req) {
        // Email ile kullanıcıyı bul
        var user = repo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));
        
        // Şifre doğrulaması yap
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("INVALID_CREDENTIALS");

        // JWT token üret ve yanıt oluştur
        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(mapper.toDto(user), token);
    }

    /**
     * Email ile mevcut kullanıcı bilgilerini getirir
     * 
     * @param email Kullanıcı email'i
     * @return Kullanıcı bilgileri
     * @throws IllegalArgumentException Kullanıcı bulunamadığında
     */
    public UserDto currentUserByEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return mapper.toDto(user);
    }

    /**
     * ID ile mevcut kullanıcı bilgilerini getirir
     * 
     * @param id Kullanıcı ID'si
     * @return Kullanıcı bilgileri
     * @throws IllegalArgumentException Kullanıcı bulunamadığında
     */
    public UserDto currentUser(UUID id) {
        return mapper.toDto(
                repo.findById(id).orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"))
        );
    }
}
