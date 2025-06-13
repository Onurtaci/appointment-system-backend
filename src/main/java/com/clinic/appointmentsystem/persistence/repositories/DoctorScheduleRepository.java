package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DoctorScheduleRepository - Doktor çalışma programı veri erişim katmanı
 * 
 * Bu repository doktor çalışma programı verilerinin veritabanı işlemlerini yönetir.
 * Program oluşturma, güncelleme, silme ve çeşitli kriterlere göre listeleme işlemlerini sağlar.
 */
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {
    
    /**
     * Belirli bir doktorun belirli bir gündeki çalışma programını getirir
     * 
     * @param doctorId Doktor ID'si
     * @param dayOfWeek Haftanın günü (MONDAY, TUESDAY, vb.)
     * @return Doktorun o günkü çalışma programı
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.dayOfWeek = :dayOfWeek AND ds.isWorkingDay = true")
    DoctorSchedule findByDoctorIdAndDayOfWeek(@Param("doctorId") UUID doctorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Belirli bir doktorun tüm çalışma günlerini getirir
     * 
     * @param doctorId Doktor ID'si
     * @return Doktorun çalışma programlarının listesi
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.isWorkingDay = true")
    List<DoctorSchedule> findAllWorkingDaysByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Belirli bir doktorun belirli bir günde ve zaman aralığında randevusu olup olmadığını kontrol eder
     * Tam gün vardiyasında öğle arası çakışması kontrolü için kullanılır
     * 
     * @param doctorId Doktor ID'si
     * @param dayOfWeek Haftanın günü
     * @param lunchBreakStart Öğle arası başlangıç saati
     * @param lunchBreakEnd Öğle arası bitiş saati
     * @return Çakışan randevu varsa true, yoksa false
     */
    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DoctorSchedule ds " +
            "WHERE ds.doctor.id = :doctorId " +
            "AND ds.dayOfWeek = :dayOfWeek " +
            "AND ds.shiftType = 'FULL_DAY' " +
            "AND ds.isWorkingDay = true " +
            "AND ((ds.startTime <= :lunchBreakStart AND ds.endTime >= :lunchBreakStart) " +
            "OR (ds.startTime <= :lunchBreakEnd AND ds.endTime >= :lunchBreakEnd) " +
            "OR (ds.startTime >= :lunchBreakStart AND ds.endTime <= :lunchBreakEnd))")
    boolean existsByDoctorIdAndTimeRange(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("lunchBreakStart") LocalTime lunchBreakStart,
            @Param("lunchBreakEnd") LocalTime lunchBreakEnd
    );
} 