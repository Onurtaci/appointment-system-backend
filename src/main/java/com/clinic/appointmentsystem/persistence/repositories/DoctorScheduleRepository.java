package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.dayOfWeek = :dayOfWeek AND ds.isWorkingDay = true")
    DoctorSchedule findByDoctorIdAndDayOfWeek(@Param("doctorId") UUID doctorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.isWorkingDay = true")
    List<DoctorSchedule> findAllWorkingDaysByDoctorId(@Param("doctorId") UUID doctorId);

    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DoctorSchedule ds " +
            "WHERE ds.doctor.id = :doctorId " +
            "AND ds.dayOfWeek = :dayOfWeek " +
            "AND ds.shiftType = 'FULL_DAY' " +
            "AND ds.isWorkingDay = true " +
            "AND ((ds.startTime <= :lunchBreakStart AND ds.endTime >= :lunchBreakStart) " +
            "OR (ds.startTime <= :lunchBreakEnd AND ds.endTime >= :lunchBreakEnd) " +
            "OR (ds.startTime >= :lunchBreakStart AND ds.endTime <= :lunchBreakEnd))")
    boolean existsByDoctorIdAndTimeRange(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("lunchBreakStart") LocalTime lunchBreakStart,
            @Param("lunchBreakEnd") LocalTime lunchBreakEnd
    );
} 