package com.clinic.appointmentsystem.application.mapper;

import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ScheduleMapper - Doktor çalışma programı dönüştürücü
 * 
 * Bu mapper DoctorSchedule entity'sini ScheduleView DTO'suna dönüştürür.
 * Zaman formatlarını ve doktor bilgilerini uygun şekilde dönüştürür.
 */
@Component
@RequiredArgsConstructor
public class ScheduleMapper {
    
    private final UserMapper userMapper;

    public ScheduleView toView(DoctorSchedule schedule) {
        return new ScheduleView(
                schedule.getId().toString(),
                userMapper.toDto(schedule.getDoctor()),
                schedule.getDayOfWeek(),
                schedule.getStartTime() != null ? schedule.getStartTime().toString() : null,
                schedule.getEndTime() != null ? schedule.getEndTime().toString() : null,
                schedule.isWorkingDay(),
                schedule.getAppointmentDurationMinutes(),
                schedule.getShiftType()
        );
    }
} 