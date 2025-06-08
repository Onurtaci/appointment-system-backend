package com.clinic.appointmentsystem.application.mapper;

import com.clinic.appointmentsystem.application.dto.appointment.AppointmentDoctorView;
import com.clinic.appointmentsystem.application.dto.appointment.AppointmentPatientView;
import com.clinic.appointmentsystem.domain.entities.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", expression = "java(a.getDoctor().getFirstName() + \" \" + a.getDoctor().getLastName())")
    @Mapping(target = "note", source = "note")
    AppointmentPatientView toPatientView(Appointment a);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(a.getPatient().getFirstName() + \" \" + a.getPatient().getLastName())")
    AppointmentDoctorView toDoctorView(Appointment a);
}