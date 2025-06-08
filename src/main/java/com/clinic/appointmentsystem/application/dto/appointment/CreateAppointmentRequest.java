package com.clinic.appointmentsystem.application.dto.appointment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        UUID patientId,
        UUID doctorId,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime appointmentTime
) {
}
