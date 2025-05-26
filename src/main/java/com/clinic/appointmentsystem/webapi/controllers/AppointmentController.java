package com.clinic.appointmentsystem.webapi.controllers;

import com.clinic.appointmentsystem.application.dto.appointment.*;
import com.clinic.appointmentsystem.application.services.AppointmentService;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import com.clinic.appointmentsystem.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    /* CREATE (patient) */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateAppointmentRequest r) {
        System.out.println("=== Create Appointment Debug ===");
        System.out.println("Request received: " + r);
        System.out.println("Appointment time: " + r.appointmentTime());
        System.out.println("Doctor ID: " + r.doctorId());
        System.out.println("Patient ID: " + r.patientId());
        
        try {
            UUID id = service.create(r);
            System.out.println("Appointment created successfully with ID: " + id);
            System.out.println("=== End Debug ===");
            return ResponseEntity.created(URI.create("/api/appointments/" + id)).body(new IdDto(id));
        } catch (Exception e) {
            System.out.println("Error creating appointment: " + e.getMessage());
            System.out.println("=== End Debug ===");
            throw e;
        }
    }

    /* PATIENT – own appointments */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentPatientView> myAppointments(Authentication auth) {
        UUID pid = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByPatient(pid);
    }

    /* DOCTOR – own appointments */
    @GetMapping("/doctor/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> myDoctorAppointments(Authentication auth) {
        UUID did = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByDoctor(did);
    }

    /* DOCTOR – specific doctor id */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> doctorAppointments(@PathVariable UUID doctorId) {
        return service.findByDoctor(doctorId);
    }

    /* STATUS PATCH */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public void updateStatus(@PathVariable UUID id, @RequestBody StatusDto dto) {
        service.updateStatus(id, AppointmentStatus.valueOf(dto.status()));
    }

    /* NOTE */
    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public void addNote(@PathVariable UUID id, @RequestBody NoteDto n) {
        service.addNote(id, n.note());
    }

    /* RESCHEDULE */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public void reschedule(@PathVariable UUID id, @RequestBody RescheduleDto r) {
        service.reschedule(id, r.appointmentTime());
    }

    /* DELETE */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    /* GET BOOKED SLOTS */
    @GetMapping("/booked-slots")
    public List<String> getBookedTimeSlots(
            @RequestParam UUID doctorId,
            @RequestParam String date) {
        try {
            // Parse the date string (YYYY-MM-DD) to LocalDate
            LocalDate localDate = LocalDate.parse(date);
            System.out.println("Received date string: " + date);
            System.out.println("Parsed to LocalDate: " + localDate);
            return service.getBookedTimeSlots(doctorId, localDate.atStartOfDay());
        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse date: " + date);
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD");
        }
    }

    /* --- Tiny DTO helpers --- */
    record IdDto(UUID id) {
    }

    record StatusDto(String status) {
    }

    record NoteDto(String note) {
    }

    record RescheduleDto(LocalDateTime appointmentTime) {
    }
}
