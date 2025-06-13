package com.clinic.appointmentsystem.domain.entities;

import com.clinic.appointmentsystem.domain.enums.ShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DoctorSchedule - Doktor çalışma programı varlık sınıfı
 * 
 * Bu sınıf doktorların haftalık çalışma programlarını temsil eder.
 * Hangi günlerde çalıştıkları, vardiya türleri ve randevu sürelerini içerir.
 */
@Entity
@Table(name = "doctor_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedule {
    
    /**
     * Programın benzersiz kimliği
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Bu programa ait doktor
     * Many-to-One ilişkisi: Bir doktor birden fazla gün için program oluşturabilir
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    /**
     * Haftanın hangi günü (MONDAY, TUESDAY, vb.)
     */
    @Column(nullable = false, updatable = false)
    private DayOfWeek dayOfWeek;

    /**
     * Vardiyanın başlangıç saati
     */
    @Column(nullable = true)
    private LocalTime startTime;

    /**
     * Vardiyanın bitiş saati
     */
    @Column(nullable = true)
    private LocalTime endTime;

    /**
     * Bu günün çalışma günü olup olmadığı
     */
    @Column(nullable = false)
    private boolean isWorkingDay;

    /**
     * Randevu süresi (dakika cinsinden)
     */
    @Column(nullable = false)
    private Integer appointmentDurationMinutes;

    /**
     * Vardiya türü (MORNING, AFTERNOON, FULL_DAY)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType;
} 