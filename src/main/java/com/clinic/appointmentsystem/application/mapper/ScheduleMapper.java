package com.clinic.appointmentsystem.application.mapper;

import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import org.springframework.stereotype.Component;

/**
 * ScheduleMapper - Doktor çalışma programı dönüştürücü
 * 
 * Bu mapper DoctorSchedule entity'sini ScheduleView DTO'suna dönüştürür.
 * Zaman formatlarını ve doktor bilgilerini uygun şekilde dönüştürür.
 */
@Component
public class ScheduleMapper {
    private ScheduleMapper() {
    }

    public static ScheduleView toView(DoctorSchedule schedule) {
        return new ScheduleView(
                schedule.getId().toString(),
                UserMapper.toDto(schedule.getDoctor()),
                schedule.getDayOfWeek(),
                schedule.getStartTime() != null ? schedule.getStartTime().toString() : null,
                schedule.getEndTime() != null ? schedule.getEndTime().toString() : null,
                schedule.isWorkingDay(),
                schedule.getAppointmentDurationMinutes(),
                schedule.getShiftType()
        );
    }
} 