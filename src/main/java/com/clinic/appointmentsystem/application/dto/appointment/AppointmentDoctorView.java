package com.clinic.appointmentsystem.application.dto.appointment;

import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentDoctorView(
        UUID id,
        UUID patientId,
        String patientName,
        LocalDateTime appointmentTime,
        AppointmentStatus status,
        String note
) {
}
