package com.clinic.appointmentsystem.application.dto.appointment;

import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentPatientView(
        UUID id,
        UUID doctorId,
        String doctorName,
        LocalDateTime appointmentTime,
        AppointmentStatus status,
        String note
) {
}
