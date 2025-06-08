package com.clinic.appointmentsystem.application.dto.schedule;

import com.clinic.appointmentsystem.domain.enums.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleView(
        UUID id,
        UUID doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean isWorkingDay,
        Integer appointmentDurationMinutes,
        ShiftType shiftType
) {
}