package com.clinic.appointmentsystem.application.dto.schedule;

import com.clinic.appointmentsystem.domain.enums.ShiftType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;

public record CreateScheduleRequest(
    @NotNull DayOfWeek dayOfWeek,
    @NotNull boolean isWorkingDay,
    @NotNull @Min(15) @Max(120) Integer appointmentDurationMinutes,
    @NotNull ShiftType shiftType
) {} 