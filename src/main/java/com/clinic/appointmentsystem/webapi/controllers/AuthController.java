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


/**
 * AuthController - Kimlik doğrulama ve yetkilendirme için REST API endpoint'leri
 * 
 * Bu controller kullanıcı kaydı, giriş ve mevcut kullanıcı bilgilerini alma işlemlerini yönetir.
 * JWT token tabanlı kimlik doğrulama sistemi kullanır.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    /**
     * Yeni kullanıcı kaydı yapar
     * 
     * @param r Kayıt isteği (ad, soyad, email, şifre, rol)
     * @return Kimlik doğrulama yanıtı (kullanıcı bilgileri ve JWT token)
     * @throws IllegalArgumentException Geçersiz veri veya email zaten kullanımda
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return service.register(r);
    }

    /**
     * Kullanıcı girişi yapar
     * 
     * @param r Giriş isteği (email, şifre)
     * @return Kimlik doğrulama yanıtı (kullanıcı bilgileri ve JWT token)
     * @throws IllegalArgumentException Geçersiz kimlik bilgileri
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    /**
     * Giriş yapmış kullanıcının bilgilerini getirir
     * 
     * @param auth Kimlik doğrulama bilgileri
     * @return Mevcut kullanıcının bilgileri
     */
    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        UserDetails principal = (UserDetails) auth.getPrincipal();
        return service.currentUserByEmail(principal.getUsername());
    }
}
