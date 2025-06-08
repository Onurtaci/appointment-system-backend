package com.clinic.appointmentsystem.application.services;

import com.clinic.appointmentsystem.application.dto.schedule.CreateScheduleRequest;
import com.clinic.appointmentsystem.application.dto.schedule.ScheduleView;
import com.clinic.appointmentsystem.application.mapper.ScheduleMapper;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import com.clinic.appointmentsystem.domain.enums.ShiftType;
import com.clinic.appointmentsystem.persistence.repositories.DoctorScheduleRepository;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleService {
    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(18, 0);
    private static final LocalTime FULL_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime FULL_DAY_END = LocalTime.of(18, 0);
    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);

    private final DoctorScheduleRepository repo;
    private final UserRepository userRepo;
    private final ScheduleMapper mapper;

    private static void setShiftTimes(DoctorSchedule schedule, ShiftType shiftType) {
        switch (shiftType) {
            case MORNING -> {
                schedule.setStartTime(DoctorScheduleService.MORNING_START);
                schedule.setEndTime(DoctorScheduleService.MORNING_END);
            }
            case AFTERNOON -> {
                schedule.setStartTime(DoctorScheduleService.AFTERNOON_START);
                schedule.setEndTime(DoctorScheduleService.AFTERNOON_END);
            }
            case FULL_DAY -> {
                schedule.setStartTime(DoctorScheduleService.FULL_DAY_START);
                schedule.setEndTime(DoctorScheduleService.FULL_DAY_END);
            }
        }
    }

    public UUID createSchedule(UUID doctorId, CreateScheduleRequest request) {
        var doctor = userRepo.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("DOCTOR_NOT_FOUND"));

        if (repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek()) != null)
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");

        var schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .isWorkingDay(request.isWorkingDay())
                .appointmentDurationMinutes(request.appointmentDurationMinutes())
                .shiftType(request.shiftType())
                .build();

        DoctorScheduleService.setShiftTimes(schedule, request.shiftType());

        if (request.shiftType() == ShiftType.FULL_DAY &&
                repo.existsByDoctorIdAndTimeRange(doctorId, request.dayOfWeek(), DoctorScheduleService.LUNCH_BREAK_START, DoctorScheduleService.LUNCH_BREAK_END))
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");

        repo.save(schedule);
        return schedule.getId();
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> getDoctorSchedule(UUID doctorId) {
        var schedules = repo.findAllWorkingDaysByDoctorId(doctorId);
        return schedules.stream()
                .map(ScheduleMapper::toView)
                .toList();
    }

    public void updateSchedule(UUID doctorId, UUID scheduleId, CreateScheduleRequest request) {
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        if (!schedule.getDoctor().getId().equals(doctorId)) throw new IllegalArgumentException("NOT_AUTHORIZED");

        var existingSchedule = repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek());
        if (existingSchedule != null && !existingSchedule.getId().equals(scheduleId))
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");

        if (request.shiftType() == ShiftType.FULL_DAY &&
                repo.existsByDoctorIdAndTimeRange(doctorId, request.dayOfWeek(), DoctorScheduleService.LUNCH_BREAK_START, DoctorScheduleService.LUNCH_BREAK_END))
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setWorkingDay(request.isWorkingDay());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setShiftType(request.shiftType());

        DoctorScheduleService.setShiftTimes(schedule, request.shiftType());
    }

    public void deleteSchedule(UUID doctorId, UUID scheduleId) {
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        if (!schedule.getDoctor().getId().equals(doctorId)) throw new IllegalArgumentException("NOT_AUTHORIZED");

        repo.delete(schedule);
    }
} 