package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.User;
import com.clinic.appointmentsystem.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository - Kullanıcı veri erişim katmanı
 * 
 * Bu repository kullanıcı verilerinin veritabanı işlemlerini yönetir.
 * Kullanıcı oluşturma, güncelleme, silme ve çeşitli kriterlere göre listeleme işlemlerini sağlar.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Email adresine göre kullanıcı arar
     * 
     * @param email Kullanıcı email adresi
     * @return Kullanıcı bulunursa Optional<User>, bulunamazsa Optional.empty()
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Belirli bir role sahip tüm kullanıcıları listeler
     * 
     * @param role Kullanıcı rolü (PATIENT, DOCTOR)
     * @return Belirtilen role sahip kullanıcıların listesi
     */
    List<User> findByRole(Role role);
}
