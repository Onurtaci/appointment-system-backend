package com.clinic.appointmentsystem.webapi.controllers;

import com.clinic.appointmentsystem.application.dto.schedule.CreateScheduleRequest;
import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.application.services.DoctorScheduleService;
import com.clinic.appointmentsystem.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DoctorScheduleController - Doktor çalışma programı yönetimi için REST API endpoint'leri
 * 
 * Bu controller doktorların çalışma saatlerini, vardiya türlerini ve randevu sürelerini yönetir.
 * Müsait saat hesaplama ve çalışma programı özeti gibi gelişmiş özellikler sağlar.
 */
@RestController
@RequestMapping("/api/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService service;

    /**
     * Yeni doktor çalışma programı oluşturur
     * 
     * @param doctorId Doktor ID'si
     * @param request Program oluşturma isteği
     * @return Oluşturulan programın ID'si
     */
    @PostMapping("/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<IdDto> createSchedule(@PathVariable UUID doctorId, @Valid @RequestBody CreateScheduleRequest request) {
        UUID id = service.createSchedule(doctorId, request);
        return ResponseEntity.created(URI.create("/api/doctor-schedules/" + id)).body(new IdDto(id));
    }

    /**
     * Doktorun çalışma programlarını listeler
     * 
     * @param doctorId Doktor ID'si
     * @return Doktorun çalışma programlarının listesi
     */
    @GetMapping("/{doctorId}")
    public List<ScheduleView> getDoctorSchedule(@PathVariable UUID doctorId) {
        return service.getDoctorSchedule(doctorId);
    }

    /**
     * Giriş yapmış doktorun çalışma programlarını listeler
     * 
     * @param auth Kimlik doğrulama bilgileri
     * @return Doktorun çalışma programlarının listesi
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<ScheduleView> mySchedule(Authentication auth) {
        UUID doctorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.getDoctorSchedule(doctorId);
    }

    /**
     * Doktor çalışma programını günceller
     * 
     * @param doctorId Doktor ID'si
     * @param scheduleId Program ID'si
     * @param request Güncelleme isteği
     */
    @PutMapping("/{doctorId}/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void updateSchedule(@PathVariable UUID doctorId, @PathVariable UUID scheduleId, @Valid @RequestBody CreateScheduleRequest request) {
        service.updateSchedule(doctorId, scheduleId, request);
    }

    /**
     * Doktor çalışma programını siler
     * 
     * @param doctorId Doktor ID'si
     * @param scheduleId Program ID'si
     */
    @DeleteMapping("/{doctorId}/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteSchedule(@PathVariable UUID doctorId, @PathVariable UUID scheduleId) {
        service.deleteSchedule(doctorId, scheduleId);
    }

    /**
     * Belirli bir doktorun belirli bir günde müsait olan zaman dilimlerini getirir
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih (YYYY-MM-DD formatında)
     * @return Müsait zaman dilimlerinin listesi
     */
    @GetMapping("/{doctorId}/available-slots")
    public List<String> getAvailableTimeSlots(@PathVariable UUID doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return service.getAvailableTimeSlots(doctorId, localDate);
    }

    /**
     * Doktorun belirli bir günde belirli bir saatte müsait olup olmadığını kontrol eder
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih (YYYY-MM-DD formatında)
     * @param time Saat (HH:mm formatında)
     * @return Müsaitlik durumu
     */
    @GetMapping("/{doctorId}/availability")
    public AvailabilityDto checkAvailability(@PathVariable UUID doctorId, @RequestParam String date, @RequestParam String time) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate timeObj = LocalDate.parse(time);
        boolean isAvailable = service.isDoctorAvailable(doctorId, localDate, timeObj.atStartOfDay().toLocalTime());
        return new AvailabilityDto(isAvailable);
    }

    /**
     * Doktorun haftalık çalışma programı özetini getirir
     * 
     * @param doctorId Doktor ID'si
     * @return Haftalık çalışma programı özeti
     */
    @GetMapping("/{doctorId}/weekly-summary")
    public WeeklySummaryDto getWeeklySummary(@PathVariable UUID doctorId) {
        String summary = service.getWeeklyScheduleSummary(doctorId);
        return new WeeklySummaryDto(summary);
    }

    // Data Transfer Objects (DTOs)
    private record IdDto(UUID id) {
    }

    private record AvailabilityDto(boolean isAvailable) {
    }

    private record WeeklySummaryDto(String summary) {
    }
} 