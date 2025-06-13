package com.clinic.appointmentsystem.application.dto.appointment;

import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * AppointmentPatientView - Hasta için randevu görünüm DTO'su
 * 
 * Bu DTO hastaların randevularını görüntülemek için kullanılır.
 * Doktor bilgileri, randevu zamanı ve durumu bilgilerini içerir.
 */
public record AppointmentPatientView(
        String id,
        UserDto doctor,
        LocalDateTime appointmentTime,
        AppointmentStatus status,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
