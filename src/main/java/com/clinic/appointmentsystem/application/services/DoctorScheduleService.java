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
    // Updated time constants to match database constraints
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

    private void setShiftTimes(DoctorSchedule schedule, ShiftType shiftType) {
        switch (shiftType) {
            case MORNING -> {
                schedule.setStartTime(MORNING_START);
                schedule.setEndTime(MORNING_END);
            }
            case AFTERNOON -> {
                schedule.setStartTime(AFTERNOON_START);
                schedule.setEndTime(AFTERNOON_END);
            }
            case FULL_DAY -> {
                schedule.setStartTime(FULL_DAY_START);
                schedule.setEndTime(FULL_DAY_END);
            }
        }
    }

    public UUID createSchedule(UUID doctorId, CreateScheduleRequest request) {
        var doctor = userRepo.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("DOCTOR_NOT_FOUND"));
        
        // Check if schedule already exists for this day
        if (repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek()) != null) {
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");
        }

        var schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .isWorkingDay(request.isWorkingDay())
                .appointmentDurationMinutes(request.appointmentDurationMinutes())
                .shiftType(request.shiftType())
                .build();

        // Set predefined times based on shift type
        setShiftTimes(schedule, request.shiftType());

        // For full day shifts, check if there are any existing appointments during lunch break
        if (request.shiftType() == ShiftType.FULL_DAY && 
            repo.existsByDoctorIdAndTimeRange(doctorId, request.dayOfWeek(), LUNCH_BREAK_START, LUNCH_BREAK_END)) {
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");
        }

        repo.save(schedule);
        return schedule.getId();
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> getDoctorSchedule(UUID doctorId) {
        System.out.println("Fetching schedule for doctor: " + doctorId);
        var schedules = repo.findAllWorkingDaysByDoctorId(doctorId);
        System.out.println("Found schedules: " + schedules.size());
        schedules.forEach(schedule -> {
            System.out.println("Schedule: " +
                "id=" + schedule.getId() +
                ", day=" + schedule.getDayOfWeek() +
                ", isWorkingDay=" + schedule.isWorkingDay() +
                ", startTime=" + schedule.getStartTime() +
                ", endTime=" + schedule.getEndTime() +
                ", shiftType=" + schedule.getShiftType()
            );
        });
        return schedules.stream()
                .map(mapper::toView)
                .toList();
    }

    public void updateSchedule(UUID doctorId, UUID scheduleId, CreateScheduleRequest request) {
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        // Verify the schedule belongs to the doctor
        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new IllegalArgumentException("NOT_AUTHORIZED");
        }

        // Check if another schedule exists for this day (excluding this one)
        var existingSchedule = repo.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek());
        if (existingSchedule != null && !existingSchedule.getId().equals(scheduleId)) {
            throw new IllegalStateException("SCHEDULE_ALREADY_EXISTS");
        }

        // For full day shifts, check if there are any existing appointments during lunch break
        if (request.shiftType() == ShiftType.FULL_DAY && 
            repo.existsByDoctorIdAndTimeRange(doctorId, request.dayOfWeek(), LUNCH_BREAK_START, LUNCH_BREAK_END)) {
            throw new IllegalStateException("EXISTING_APPOINTMENTS_DURING_LUNCH_BREAK");
        }

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setWorkingDay(request.isWorkingDay());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setShiftType(request.shiftType());

        // Update times based on shift type
        setShiftTimes(schedule, request.shiftType());
    }

    public void deleteSchedule(UUID doctorId, UUID scheduleId) {
        var schedule = repo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("SCHEDULE_NOT_FOUND"));

        // Verify the schedule belongs to the doctor
        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new IllegalArgumentException("NOT_AUTHORIZED");
        }

        repo.delete(schedule);
    }
} 