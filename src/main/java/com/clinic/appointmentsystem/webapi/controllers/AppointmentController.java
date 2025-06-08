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

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<IdDto> create(@Valid @RequestBody CreateAppointmentRequest r) {
        UUID id = service.create(r);
        return ResponseEntity.created(URI.create("/api/appointments/" + id)).body(new IdDto(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentPatientView> myAppointments(Authentication auth) {
        UUID pid = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByPatient(pid);
    }

    @GetMapping("/doctor/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> myDoctorAppointments(Authentication auth) {
        UUID did = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.findByDoctor(did);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentDoctorView> doctorAppointments(@PathVariable UUID doctorId) {
        return service.findByDoctor(doctorId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public void updateStatus(@PathVariable UUID id, @RequestBody StatusDto dto) {
        service.updateStatus(id, AppointmentStatus.valueOf(dto.status()));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public void addNote(@PathVariable UUID id, @RequestBody NoteDto n) {
        service.addNote(id, n.note());
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public void reschedule(@PathVariable UUID id, @RequestBody RescheduleDto r) {
        service.reschedule(id, r.appointmentTime());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/booked-slots")
    public List<String> getBookedTimeSlots(@RequestParam UUID doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return service.getBookedTimeSlots(doctorId, localDate.atStartOfDay());

    }

    private record IdDto(UUID id) {
    }

    record StatusDto(String status) {
    }

    record NoteDto(String note) {
    }

    record RescheduleDto(LocalDateTime appointmentTime) {
    }
}
