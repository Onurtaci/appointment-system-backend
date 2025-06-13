package com.clinic.appointmentsystem.domain.entities;

import com.clinic.appointmentsystem.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User - Kullanıcı varlık sınıfı
 * 
 * Bu sınıf sistemdeki tüm kullanıcıları (hasta ve doktor) temsil eder.
 * Kullanıcı bilgileri, kimlik doğrulama verileri ve rol bilgilerini içerir.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /**
     * Kullanıcının benzersiz kimliği
     */
    @Id
    private UUID id;
    
    /**
     * Kullanıcının adı
     */
    private String firstName;
    
    /**
     * Kullanıcının soyadı
     */
    private String lastName;
    
    /**
     * Kullanıcının email adresi (benzersiz)
     */
    private String email;
    
    /**
     * Şifrenin hash'lenmiş hali (BCrypt ile şifrelenmiş)
     */
    private String passwordHash;
    
    /**
     * Kullanıcının rolü (PATIENT veya DOCTOR)
     */
    @Enumerated(EnumType.STRING)
    private Role role;
    
    /**
     * Kullanıcının sisteme kayıt tarihi
     */
    private LocalDateTime createdAt;
    
    /**
     * Kullanıcı bilgilerinin son güncellenme tarihi
     */
    private LocalDateTime updatedAt;
}
