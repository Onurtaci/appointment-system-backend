package com.clinic.appointmentsystem.application.mapper;

import com.clinic.appointmentsystem.application.dto.appointment.AppointmentDoctorView;
import com.clinic.appointmentsystem.application.dto.appointment.AppointmentPatientView;
import com.clinic.appointmentsystem.domain.entities.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AppointmentMapper - Randevu dönüştürücü
 * 
 * Bu mapper Appointment entity'sini çeşitli DTO'lara dönüştürür.
 * Doktor ve hasta görünümleri için farklı mapping'ler sağlar.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AppointmentMapper {
    
    @Mapping(target = "id", expression = "java(appointment.getId().toString())")
    @Mapping(target = "doctor", source = "doctor")
    @Mapping(target = "appointmentTime", source = "appointmentTime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AppointmentPatientView toPatientView(Appointment appointment);

    @Mapping(target = "id", expression = "java(appointment.getId().toString())")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "appointmentTime", source = "appointmentTime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AppointmentDoctorView toDoctorView(Appointment appointment);
}