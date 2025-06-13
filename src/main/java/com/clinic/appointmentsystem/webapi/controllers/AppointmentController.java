package com.clinic.appointmentsystem.webapi.controllers;

import com.clinic.appointmentsystem.application.dto.appointment.AppointmentDoctorView;
import com.clinic.appointmentsystem.application.dto.appointment.AppointmentPatientView;
import com.clinic.appointmentsystem.application.dto.appointment.CreateAppointmentRequest;
import com.clinic.appointmentsystem.application.services.AppointmentService;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import com.clinic.appointmentsystem.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AppointmentController - Randevu yönetimi için REST API endpoint'leri
 * 
 * Bu controller randevu oluşturma, listeleme, güncelleme ve silme işlemlerini yönetir.
 * Hasta ve doktor rollerine göre farklı endpoint'ler sağlar.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    /**
     * Yeni randevu oluşturur
     * 
     * @param r Randevu oluşturma isteği (doktor ID, hasta ID, randevu zamanı)
     * @return Oluşturulan randevunun ID'si
     * @throws IllegalArgumentException Geçersiz randevu zamanı veya çakışma durumunda
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<IdDto> create(@Valid @RequestBody CreateAppointmentRequest r) {
        UUID id = service.create(r);
        return ResponseEntity.created(URI.create("/api/appointments/" + id)).body(new IdDto(id));
    }

    /**
     * Giriş yapmış hastanın randevularını listeler
     * 
     * @param auth Kimlik doğrulama bilgileri
     * @return Hasta randevularının listesi
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentPatientView> myAppointments(Authentication auth) {
        UUID pid = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByPatient(pid);
    }

    /**
     * Giriş yapmış doktorun randevularını listeler
     * 
     * @param auth Kimlik doğrulama bilgileri
     * @return Doktor randevularının listesi
     */
    @GetMapping("/doctor/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> myDoctorAppointments(Authentication auth) {
        UUID did = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByDoctor(did);
    }

    /**
     * Belirli bir doktorun randevularını listeler
     * 
     * @param doctorId Doktor ID'si
     * @return Doktor randevularının listesi
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> doctorAppointments(@PathVariable UUID doctorId) {
        return service.findByDoctor(doctorId);
    }

    /**
     * Randevu durumunu günceller (onaylama/reddetme)
     * 
     * @param id Randevu ID'si
     * @param dto Yeni durum bilgisi
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public void updateStatus(@PathVariable UUID id, @RequestBody StatusDto dto) {
        service.updateStatus(id, AppointmentStatus.valueOf(dto.status()));
    }

    /**
     * Randevuya not ekler
     * 
     * @param id Randevu ID'si
     * @param n Not içeriği
     */
    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public void addNote(@PathVariable UUID id, @RequestBody NoteDto n) {
        service.addNote(id, n.note());
    }

    /**
     * Randevuyu yeniden planlar
     * 
     * @param id Randevu ID'si
     * @param r Yeni randevu zamanı
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public void reschedule(@PathVariable UUID id, @RequestBody RescheduleDto r) {
        service.reschedule(id, r.appointmentTime());
    }

    /**
     * Randevuyu siler
     * 
     * @param id Randevu ID'si
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    /**
     * Belirli bir doktorun belirli bir günde dolu olan zaman dilimlerini getirir
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih (YYYY-MM-DD formatında)
     * @return Dolu zaman dilimlerinin listesi
     */
    @GetMapping("/booked-slots")
    public List<String> getBookedTimeSlots(@RequestParam UUID doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return service.getBookedTimeSlots(doctorId, localDate.atStartOfDay());
    }

    /**
     * Belirli bir doktorun belirli bir günde müsait olan zaman dilimlerini getirir
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih (YYYY-MM-DD formatında)
     * @return Müsait zaman dilimlerinin listesi
     */
    @GetMapping("/available-slots")
    public List<String> getAvailableTimeSlots(@RequestParam UUID doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return service.getAvailableTimeSlots(doctorId, localDate);
    }

    // Data Transfer Objects (DTOs)
    private record IdDto(UUID id) {
    }

    record StatusDto(String status) {
    }

    record NoteDto(String note) {
    }

    record RescheduleDto(LocalDateTime appointmentTime) {
    }
}
