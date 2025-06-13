package com.clinic.appointmentsystem.application.dto.schedule;

import com.clinic.appointmentsystem.application.dto.user.UserDto;
import com.clinic.appointmentsystem.domain.enums.ShiftType;

import java.time.DayOfWeek;

/**
 * ScheduleView - Doktor çalışma programı görünüm DTO'su
 * 
 * Bu DTO doktor çalışma programı bilgilerini frontend'e döndürmek için kullanılır.
 * Doktor bilgileri, çalışma saatleri ve randevu süresi bilgilerini içerir.
 */
public record ScheduleView(
        String id,
        UserDto doctor,
        DayOfWeek dayOfWeek,
        String startTime,
        String endTime,
        boolean isWorkingDay,
        Integer appointmentDurationMinutes,
        ShiftType shiftType
) {
}