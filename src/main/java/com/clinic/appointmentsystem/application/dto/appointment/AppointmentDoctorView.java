package com.clinic.appointmentsystem.application.dto.appointment;

import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * AppointmentDoctorView - Doktor için randevu görünüm DTO'su
 * 
 * Bu DTO doktorların randevularını görüntülemek için kullanılır.
 * Hasta bilgileri, randevu zamanı ve durumu bilgilerini içerir.
 */
public record AppointmentDoctorView(
        String id,
        UserDto patient,
        LocalDateTime appointmentTime,
        AppointmentStatus status,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
