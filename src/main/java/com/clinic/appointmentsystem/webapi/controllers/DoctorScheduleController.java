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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService service;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createSchedule(
            Authentication auth,
            @Valid @RequestBody CreateScheduleRequest request) {
        UUID doctorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        UUID id = service.createSchedule(doctorId, request);
        return ResponseEntity.created(URI.create("/api/doctor-schedules/" + id)).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<ScheduleView> getMySchedule(Authentication auth) {
        UUID doctorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        return service.getDoctorSchedule(doctorId);
    }

    @GetMapping("/{doctorId}")
    public List<ScheduleView> getDoctorSchedule(@PathVariable UUID doctorId) {
        return service.getDoctorSchedule(doctorId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void updateSchedule(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody CreateScheduleRequest request) {
        UUID doctorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        service.updateSchedule(doctorId, id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteSchedule(Authentication auth, @PathVariable UUID id) {
        UUID doctorId = ((CustomUserDetails) auth.getPrincipal()).getId();
        service.deleteSchedule(doctorId, id);
    }
} 