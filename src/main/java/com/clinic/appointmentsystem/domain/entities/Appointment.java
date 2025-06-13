package com.clinic.appointmentsystem.domain.entities;

import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Appointment - Randevu varlık sınıfı
 * 
 * Bu sınıf hasta ve doktor arasındaki randevuları temsil eder.
 * Randevu zamanı, durumu, notları ve ilişkili kullanıcı bilgilerini içerir.
 */
@Entity
@Table(name = "appointments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "appointment_time"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    
    /**
     * Randevunun benzersiz kimliği
     */
    @Id
    private UUID id;

    /**
     * Randevuyu alan hasta
     * Many-to-One ilişkisi: Bir hasta birden fazla randevu alabilir
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private User patient;

    /**
     * Randevuyu veren doktor
     * Many-to-One ilişkisi: Bir doktor birden fazla randevu verebilir
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    /**
     * Randevunun tarihi ve saati
     */
    private LocalDateTime appointmentTime;

    /**
     * Randevunun durumu (PENDING, APPROVED, REJECTED)
     */
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    /**
     * Doktorun randevu hakkında eklediği notlar
     */
    private String note;
    
    /**
     * Randevunun oluşturulma tarihi
     */
    private LocalDateTime createdAt;
    
    /**
     * Randevu bilgilerinin son güncellenme tarihi
     */
    private LocalDateTime updatedAt;
}
