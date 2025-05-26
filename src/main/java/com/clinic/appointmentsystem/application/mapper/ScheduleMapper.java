package com.clinic.appointmentsystem.application.mapper;

import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {
    public ScheduleView toView(DoctorSchedule schedule) {
        return new ScheduleView(
            schedule.getId(),
            schedule.getDoctor().getId(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.isWorkingDay(),
            schedule.getAppointmentDurationMinutes(),
            schedule.getShiftType()
        );
    }
} 