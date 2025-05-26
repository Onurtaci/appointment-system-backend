package com.clinic.appointmentsystem.application.services;

import com.clinic.appointmentsystem.application.dto.appointment.AppointmentDoctorView;
import com.clinic.appointmentsystem.application.dto.appointment.AppointmentPatientView;
import com.clinic.appointmentsystem.application.dto.appointment.CreateAppointmentRequest;
import com.clinic.appointmentsystem.application.mapper.AppointmentMapper;
import com.clinic.appointmentsystem.domain.entities.Appointment;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import com.clinic.appointmentsystem.persistence.repositories.AppointmentRepository;
import com.clinic.appointmentsystem.persistence.repositories.DoctorScheduleRepository;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final AppointmentMapper mapper;
    private final DoctorScheduleRepository scheduleRepo;

    /* --- CREATE --- */
    public UUID create(CreateAppointmentRequest r) {
        if (r.appointmentTime().isBefore(LocalDateTime.now())) throw new IllegalArgumentException("APPT_PAST_DATE");
        
        // Check if the doctor works on this day
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(r.doctorId(), r.appointmentTime().getDayOfWeek());
        if (schedule == null || !schedule.isWorkingDay()) {
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");
        }

        // Check if the appointment time is within working hours
        LocalTime appointmentTime = r.appointmentTime().toLocalTime();
        if (appointmentTime.isBefore(schedule.getStartTime()) || appointmentTime.isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException("APPT_OUTSIDE_WORKING_HOURS");
        }

        // Check if appointment is during lunch break for full-day shifts
        if (schedule.getShiftType() == DoctorSchedule.ShiftType.FULL_DAY) {
            LocalTime appointmentEndTime = appointmentTime.plusMinutes(schedule.getAppointmentDurationMinutes());
            if ((appointmentTime.isAfter(LUNCH_BREAK_START) && appointmentTime.isBefore(LUNCH_BREAK_END)) ||
                (appointmentEndTime.isAfter(LUNCH_BREAK_START) && appointmentEndTime.isBefore(LUNCH_BREAK_END))) {
                throw new IllegalArgumentException("APPT_DURING_LUNCH_BREAK");
            }
        }

        if (repo.existsByDoctorIdAndAppointmentTime(r.doctorId(), r.appointmentTime()))
            throw new IllegalStateException("APPT_TIME_SLOT_BOOKED");

        var patient = userRepo.findById(r.patientId()).orElseThrow(() -> new IllegalArgumentException("PATIENT_NOT_FOUND"));
        var doctor = userRepo.findById(r.doctorId()).orElseThrow(() -> new IllegalArgumentException("DOCTOR_NOT_FOUND"));

        var a = Appointment.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(r.appointmentTime())
                .status(AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repo.save(a);
        return a.getId();
    }

    /* --- QUERIES --- */
    @Transactional(readOnly = true)
    public List<AppointmentPatientView> findByPatient(UUID patientId) {
        return repo.findByPatientId(patientId).stream().map(mapper::toPatientView).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDoctorView> findByDoctor(UUID doctorId) {
        return repo.findByDoctorId(doctorId).stream().map(mapper::toDoctorView).toList();
    }

    /* --- STATUS UPDATE --- */
    public void updateStatus(UUID id, AppointmentStatus status) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        appt.setStatus(status);
        appt.setUpdatedAt(LocalDateTime.now());
    }

    /* --- NOTE --- */
    public void addNote(UUID id, String note) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        appt.setNote(note);
        appt.setUpdatedAt(LocalDateTime.now());
    }

    /* --- RESCHEDULE --- */
    public void reschedule(UUID id, LocalDateTime newTime) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        if (newTime.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("APPT_PAST_DATE");

        // Check if the doctor works on this day
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(appt.getDoctor().getId(), newTime.getDayOfWeek());
        if (schedule == null || !schedule.isWorkingDay()) {
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");
        }

        // Check if the appointment time is within working hours
        LocalTime appointmentTime = newTime.toLocalTime();
        if (appointmentTime.isBefore(schedule.getStartTime()) || appointmentTime.isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException("APPT_OUTSIDE_WORKING_HOURS");
        }

        // Check if appointment is during lunch break for full-day shifts
        if (schedule.getShiftType() == DoctorSchedule.ShiftType.FULL_DAY) {
            LocalTime appointmentEndTime = appointmentTime.plusMinutes(schedule.getAppointmentDurationMinutes());
            if ((appointmentTime.isAfter(LUNCH_BREAK_START) && appointmentTime.isBefore(LUNCH_BREAK_END)) ||
                (appointmentEndTime.isAfter(LUNCH_BREAK_START) && appointmentEndTime.isBefore(LUNCH_BREAK_END))) {
                throw new IllegalArgumentException("APPT_DURING_LUNCH_BREAK");
            }
        }

        if (repo.existsByDoctorIdAndAppointmentTime(appt.getDoctor().getId(), newTime))
            throw new IllegalStateException("APPT_TIME_SLOT_BOOKED");

        appt.setAppointmentTime(newTime);
        appt.setUpdatedAt(LocalDateTime.now());
        appt.setStatus(AppointmentStatus.PENDING);
    }

    /* --- DELETE --- */
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("APPT_NOT_FOUND");
        repo.deleteById(id);
    }

    /* --- GET BOOKED SLOTS --- */
    @Transactional(readOnly = true)
    public List<String> getBookedTimeSlots(UUID doctorId, LocalDateTime date) {
        // Convert UTC date to local date using system default timezone
        LocalDateTime localDate = date.atZone(java.time.ZoneOffset.UTC)
            .withZoneSameInstant(java.time.ZoneId.systemDefault())
            .toLocalDateTime();
            
        System.out.println("=== Booked Time Slots Debug ===");
        System.out.println("Input date (UTC): " + date);
        System.out.println("Local date: " + localDate);
        System.out.println("Local day of week: " + localDate.getDayOfWeek());
        
        // Check if the doctor works on this day
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctorId, localDate.getDayOfWeek());
        System.out.println("Doctor schedule for " + localDate.getDayOfWeek() + ": " + schedule);
        
        if (schedule == null || !schedule.isWorkingDay()) {
            System.out.println("Doctor is not working on this day");
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");
        }

        // Get start and end of the day based on doctor's schedule
        LocalDateTime startOfDay = localDate.toLocalDate().atTime(schedule.getStartTime());
        LocalDateTime endOfDay = localDate.toLocalDate().atTime(schedule.getEndTime());
        System.out.println("Checking appointments between " + startOfDay + " and " + endOfDay);

        // Get all appointments for the doctor on the given date
        List<Appointment> appointments = repo.findByDoctorIdAndDateRange(doctorId, startOfDay, endOfDay);
        System.out.println("Found " + appointments.size() + " appointments for this time range");
        
        if (appointments.isEmpty()) {
            System.out.println("No appointments found for this date");
        } else {
            System.out.println("Appointments found:");
            appointments.forEach(appt -> {
                System.out.println("  - Appointment: " + appt.getAppointmentTime() + 
                    ", Status: " + appt.getStatus() + 
                    ", Patient: " + appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName());
            });
        }

        // Convert appointment times to time slot strings (HH:mm format)
        List<String> bookedSlots = appointments.stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime().toString().substring(0, 5))
                .toList();
        System.out.println("Booked slots (HH:mm format): " + bookedSlots);
        System.out.println("=== End Debug ===");
        
        return bookedSlots;
    }
}
