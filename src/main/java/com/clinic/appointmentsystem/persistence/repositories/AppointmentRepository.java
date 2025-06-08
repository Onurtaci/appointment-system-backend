package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.Appointment;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByDoctorIdAndAppointmentTime(UUID doctorId, LocalDateTime appointmentTime);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId")
    List<Appointment> findByDoctorId(@Param("doctorId") UUID doctorId);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId AND a.status = :status")
    List<Appointment> findByDoctorIdAndStatus(@Param("doctorId") UUID doctorId, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentTime BETWEEN :startDate AND :endDate " +
            "AND a.status != 'REJECTED'")
    List<Appointment> findByDoctorIdAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
