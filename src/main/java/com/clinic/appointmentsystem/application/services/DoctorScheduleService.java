package com.clinic.appointmentsystem.application.services;

import com.clinic.appointmentsystem.application.dto.schedule.CreateScheduleRequest;
import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.application.mapper.ScheduleMapper;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import com.clinic.appointmentsystem.domain.enums.ShiftType;
import com.clinic.appointmentsystem.persistence.repositories.AppointmentRepository;
import com.clinic.appointmentsystem.persistence.repositories.DoctorScheduleRepository;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DoctorScheduleService - Doktor çalışma programı yönetim servisi
 * 
 * Bu servis doktorların çalışma saatlerini, vardiya türlerini ve randevu sürelerini yönetir.
 * Sabah, öğleden sonra ve tam gün vardiyalarını destekler ve öğle arası kontrolü yapar.
 * Müsait saat hesaplama ve esnek çalışma saatleri yönetimi sağlar.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleService {
    // Sabah vardiyası saatleri (09:00-12:00)
    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    
    // Öğleden sonra vardiyası saatleri (13:00-18:00)
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(18, 0);
    
    // Tam gün vardiyası saatleri (09:00-18:00)
    private static final LocalTime FULL_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime FULL_DAY_END = LocalTime.of(18, 0);
    
    // Öğle arası zaman dilimi (12:00-13:00)
    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);

    private final DoctorScheduleRepository repo;
    private final UserRepository userRepo;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentRepository appointmentRepo;

    /**
     * Vardiya türüne göre başlangıç ve bitiş saatlerini ayarlar
     * 
     * @param schedule Program nesnesi
     * @param shiftType Vardiya türü (MORNING, AFTERNOON, FULL_DAY)
     */
    private static void setShiftTimes(DoctorSchedule schedule, ShiftType shiftType) {
        switch (shiftType) {
            case MORNING -> {
                // Sabah vardiyası: 09:00-12:00
                schedule.setStartTime(DoctorScheduleService.MORNING_START);
                schedule.setEndTime(DoctorScheduleService.MORNING_END);
            }
            case AFTERNOON -> {
                // Öğleden sonra vardiyası: 13:00-18:00
                schedule.setStartTime(DoctorScheduleService.AFTERNOON_START);
                schedule.setEndTime(DoctorScheduleService.AFTERNOON_END);
            }
            case FULL_DAY -> {
                // Tam gün vardiyası: 09:00-18:00 (öğle arası hariç)
                schedule.setStartTime(DoctorScheduleService.FULL_DAY_START);
                schedule.setEndTime(DoctorScheduleService.FULL_DAY_END);
            }
        }
    }

    /**
     * Yeni doktor çalışma programı oluşturur
     * 
     * @param doctorId Doktor ID'si
     * @param request Program oluşturma isteği
     * @return Oluşturulan programın ID'si
     * @throws IllegalArgumentException Doktor bulunamadığında veya program zaten mevcut olduğunda
     * @throws IllegalStateException Tam gün vardiyasında öğle arası çakışması olduğunda
     */
    public UUID createSchedule(UUID doctorId, CreateScheduleRequest request) {
        // Doktorun varlığını kontrol et
        var doctor = userRepo.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("DOCTOR_NOT_FOUND"));

        // Aynı gün için zaten program var mı kontrol et
        if (repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek()) != null)
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");

        // Randevu süresi validasyonu
        validateAppointmentDuration(request.appointmentDurationMinutes());

        // Yeni program nesnesi oluştur
        var schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .isWorkingDay(request.isWorkingDay())
                .appointmentDurationMinutes(request.appointmentDurationMinutes())
                .shiftType(request.shiftType())
                .build();

        // Vardiya türüne göre saatleri ayarla
        DoctorScheduleService.setShiftTimes(schedule, request.shiftType());

        // Tam gün vardiyasında öğle arası çakışması kontrolü
        if (request.shiftType() == ShiftType.FULL_DAY) {
            // Öğle arası için randevu çakışması kontrolü
            // Bu kontrol için örnek bir tarih kullanıyoruz (bugünün tarihi)
            LocalDate today = LocalDate.now();
            LocalDateTime lunchStart = LocalDateTime.of(today, LUNCH_BREAK_START);
            LocalDateTime lunchEnd = LocalDateTime.of(today, LUNCH_BREAK_END);
            
            if (appointmentRepo.existsByDoctorIdAndTimeRange(doctorId, lunchStart, lunchEnd, request.appointmentDurationMinutes())) {
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");
            }
        }

        // Programı kaydet
        repo.save(schedule);
        return schedule.getId();
    }

    /**
     * Doktorun çalışma programlarını listeler
     * 
     * @param doctorId Doktor ID'si
     * @return Doktorun çalışma programlarının listesi
     */
    @Transactional(readOnly = true)
    public List<ScheduleView> getDoctorSchedule(UUID doctorId) {
        // Sadece çalışma günlerini getir
        var schedules = repo.findAllWorkingDaysByDoctorId(doctorId);
        return schedules.stream()
                .map(scheduleMapper::toView)
                .toList();
    }

    /**
     * Doktor çalışma programını günceller
     * 
     * @param doctorId Doktor ID'si
     * @param scheduleId Program ID'si
     * @param request Güncelleme isteği
     * @throws IllegalArgumentException Program bulunamadığında veya yetkisiz erişimde
     * @throws IllegalStateException Aynı gün için başka program mevcut olduğunda
     */
    public void updateSchedule(UUID doctorId, UUID scheduleId, CreateScheduleRequest request) {
        // Programın varlığını kontrol et
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        // Doktorun kendi programını güncellediğini kontrol et
        if (!schedule.getDoctor().getId().equals(doctorId)) throw new IllegalArgumentException("NOT_AUTHORIZED");

        // Aynı gün için başka program var mı kontrol et
        var existingSchedule = repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek());
        if (existingSchedule != null && !existingSchedule.getId().equals(scheduleId))
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");

        // Randevu süresi validasyonu
        validateAppointmentDuration(request.appointmentDurationMinutes());

        // Tam gün vardiyasında öğle arası çakışması kontrolü
        if (request.shiftType() == ShiftType.FULL_DAY) {
            // Öğle arası için randevu çakışması kontrolü
            // Bu kontrol için örnek bir tarih kullanıyoruz (bugünün tarihi)
            LocalDate today = LocalDate.now();
            LocalDateTime lunchStart = LocalDateTime.of(today, LUNCH_BREAK_START);
            LocalDateTime lunchEnd = LocalDateTime.of(today, LUNCH_BREAK_END);
            
            if (appointmentRepo.existsByDoctorIdAndTimeRange(doctorId, lunchStart, lunchEnd, request.appointmentDurationMinutes())) {
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");
            }
        }

        // Program bilgilerini güncelle
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setWorkingDay(request.isWorkingDay());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setShiftType(request.shiftType());

        // Vardiya türüne göre saatleri ayarla
        DoctorScheduleService.setShiftTimes(schedule, request.shiftType());
    }

    /**
     * Doktor çalışma programını siler
     * 
     * @param doctorId Doktor ID'si
     * @param scheduleId Program ID'si
     * @throws IllegalArgumentException Program bulunamadığında veya yetkisiz erişimde
     */
    public void deleteSchedule(UUID doctorId, UUID scheduleId) {
        // Programın varlığını kontrol et
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        // Doktorun kendi programını sildiğini kontrol et
        if (!schedule.getDoctor().getId().equals(doctorId)) throw new IllegalArgumentException("NOT_AUTHORIZED");

        // Programı sil
        repo.delete(schedule);
    }

    /**
     * Belirli bir doktorun belirli bir günde müsait olan zaman dilimlerini hesaplar
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih
     * @return Müsait zaman dilimlerinin listesi (HH:mm formatında)
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableTimeSlots(UUID doctorId, LocalDate date) {
        // Doktorun o günkü programını al
        DoctorSchedule schedule = repo.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());
        
        if (schedule == null || !schedule.isWorkingDay()) {
            return new ArrayList<>(); // Çalışma günü değil
        }

        List<String> availableSlots = new ArrayList<>();
        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int durationMinutes = schedule.getAppointmentDurationMinutes();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) || 
               currentTime.plusMinutes(durationMinutes).equals(endTime)) {
            
            // Tam gün vardiyasında öğle arası kontrolü
            if (schedule.getShiftType() == ShiftType.FULL_DAY) {
                LocalTime slotEndTime = currentTime.plusMinutes(durationMinutes);
                
                // Öğle arası ile çakışma kontrolü
                boolean conflictsWithLunch = (currentTime.isBefore(LUNCH_BREAK_END) && 
                                            slotEndTime.isAfter(LUNCH_BREAK_START));
                
                if (!conflictsWithLunch) {
                    availableSlots.add(currentTime.format(formatter));
                }
            } else {
                // Sabah veya öğleden sonra vardiyası için öğle arası kontrolü gerekmez
                availableSlots.add(currentTime.format(formatter));
            }
            
            // Bir sonraki zaman dilimine geç
            currentTime = currentTime.plusMinutes(durationMinutes);
        }

        return availableSlots;
    }

    /**
     * Doktorun çalışma saatlerini kontrol eder
     * 
     * @param doctorId Doktor ID'si
     * @param appointmentTime Randevu zamanı
     * @return Doktorun o saatte çalışıp çalışmadığı
     */
    @Transactional(readOnly = true)
    public boolean isDoctorAvailable(UUID doctorId, LocalDate date, LocalTime time) {
        DoctorSchedule schedule = repo.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());
        
        if (schedule == null || !schedule.isWorkingDay()) {
            return false;
        }

        // Çalışma saatleri kontrolü
        if (time.isBefore(schedule.getStartTime()) || time.isAfter(schedule.getEndTime())) {
            return false;
        }

        // Tam gün vardiyasında öğle arası kontrolü
        if (schedule.getShiftType() == ShiftType.FULL_DAY) {
            LocalTime appointmentEndTime = time.plusMinutes(schedule.getAppointmentDurationMinutes());
            
            // Öğle arası ile çakışma kontrolü
            boolean conflictsWithLunch = (time.isBefore(LUNCH_BREAK_END) && 
                                        appointmentEndTime.isAfter(LUNCH_BREAK_START));
            
            if (conflictsWithLunch) {
                return false;
            }
        }

        return true;
    }

    /**
     * Randevu süresi validasyonu
     * 
     * @param durationMinutes Randevu süresi (dakika)
     * @throws IllegalArgumentException Geçersiz süre
     */
    private void validateAppointmentDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("INVALID_APPOINTMENT_DURATION");
        }
        
        // Minimum 15 dakika, maksimum 120 dakika
        if (durationMinutes < 15 || durationMinutes > 120) {
            throw new IllegalArgumentException("APPOINTMENT_DURATION_OUT_OF_RANGE");
        }
        
        // 15 dakikalık katları olmalı
        if (durationMinutes % 15 != 0) {
            throw new IllegalArgumentException("APPOINTMENT_DURATION_MUST_BE_MULTIPLE_OF_15");
        }
    }

    /**
     * Doktorun haftalık çalışma programını özet olarak getirir
     * 
     * @param doctorId Doktor ID'si
     * @return Haftalık çalışma programı özeti
     */
    @Transactional(readOnly = true)
    public String getWeeklyScheduleSummary(UUID doctorId) {
        var schedules = repo.findAllWorkingDaysByDoctorId(doctorId);
        
        if (schedules.isEmpty()) {
            return "Bu doktor henüz çalışma programı oluşturmamış.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Haftalık Çalışma Programı:\n");
        
        for (var schedule : schedules) {
            summary.append(String.format("%s: %s - %s (%s, %d dakika)\n",
                    schedule.getDayOfWeek().toString(),
                    schedule.getStartTime().toString(),
                    schedule.getEndTime().toString(),
                    schedule.getShiftType().toString(),
                    schedule.getAppointmentDurationMinutes()));
        }
        
        return summary.toString();
    }
} 