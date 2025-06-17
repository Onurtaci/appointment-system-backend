package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AppointmentRepository - Randevu veri erişim katmanı
 * 
 * Bu repository randevu verilerinin veritabanı işlemlerini yönetir.
 * Randevu oluşturma, güncelleme, silme ve çeşitli kriterlere göre listeleme işlemlerini sağlar.
 * Gelişmiş çakışma kontrolü ve zaman aralığı sorguları içerir.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    /**
     * Belirli bir doktorun belirli bir saatte randevusu olup olmadığını kontrol eder
     * 
     * @param doctorId Doktor ID'si
     * @param appointmentTime Randevu zamanı
     * @return Randevu varsa true, yoksa false
     */
    boolean existsByDoctorIdAndAppointmentTime(UUID doctorId, LocalDateTime appointmentTime);

    /**
     * Belirli bir hastanın tüm randevularını getirir
     * Hasta ve doktor bilgileriyle birlikte (LEFT JOIN FETCH)
     * 
     * @param patientId Hasta ID'si
     * @return Hasta randevularının listesi
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") UUID patientId);

    /**
     * Belirli bir doktorun tüm randevularını getirir
     * Hasta ve doktor bilgileriyle birlikte (LEFT JOIN FETCH)
     * 
     * @param doctorId Doktor ID'si
     * @return Doktor randevularının listesi
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId")
    List<Appointment> findByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Belirli bir doktorun belirli tarih aralığındaki randevularını getirir
     * Reddedilmiş randevular hariç tutulur
     * Hasta ve doktor bilgileriyle birlikte (LEFT JOIN FETCH)
     * 
     * @param doctorId Doktor ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Tarih aralığındaki randevuların listesi
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentTime BETWEEN :startDate AND :endDate " +
            "AND a.status != 'REJECTED'")
    List<Appointment> findByDoctorIdAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Belirli bir doktorun belirli zaman aralığında randevu çakışması olup olmadığını kontrol eder
     * 
     * @param doctorId Doktor ID'si
     * @param startTime Başlangıç zamanı
     * @param endTime Bitiş zamanı
     * @param durationMinutes Randevu süresi (dakika)
     * @return Çakışma varsa true, yoksa false
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM appointments a " +
            "WHERE a.doctor_id = :doctorId " +
            "AND a.status != 'REJECTED' " +
            "AND ((a.appointment_time >= :startTime AND a.appointment_time < :endTime) " +
            "OR (a.appointment_time <= :startTime AND a.appointment_time + INTERVAL '1 minute' * :durationMinutes > :startTime))",
            nativeQuery = true)
    boolean existsByDoctorIdAndTimeRange(
            @Param("doctorId") UUID doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("durationMinutes") int durationMinutes
    );

    /**
     * Belirli bir doktorun belirli zaman aralığında randevu çakışması olup olmadığını kontrol eder
     * Belirli bir randevu hariç tutulur (yeniden planlama için)
     * 
     * @param doctorId Doktor ID'si
     * @param startTime Başlangıç zamanı
     * @param endTime Bitiş zamanı
     * @param durationMinutes Randevu süresi (dakika)
     * @param excludeAppointmentId Hariç tutulacak randevu ID'si
     * @return Çakışma varsa true, yoksa false
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM appointments a " +
            "WHERE a.doctor_id = :doctorId " +
            "AND a.id != :excludeAppointmentId " +
            "AND a.status != 'REJECTED' " +
            "AND ((a.appointment_time >= :startTime AND a.appointment_time < :endTime) " +
            "OR (a.appointment_time <= :startTime AND a.appointment_time + INTERVAL '1 minute' * :durationMinutes > :startTime))",
            nativeQuery = true)
    boolean existsByDoctorIdAndTimeRangeExcludingAppointment(
            @Param("doctorId") UUID doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("durationMinutes") int durationMinutes,
            @Param("excludeAppointmentId") UUID excludeAppointmentId
    );
}
