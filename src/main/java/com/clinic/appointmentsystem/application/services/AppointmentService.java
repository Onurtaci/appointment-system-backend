package com.clinic.appointmentsystem.application.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.appointmentsystem.application.dto.appointment.AppointmentDoctorView;
import com.clinic.appointmentsystem.application.dto.appointment.AppointmentPatientView;
import com.clinic.appointmentsystem.application.dto.appointment.CreateAppointmentRequest;
import com.clinic.appointmentsystem.application.mapper.AppointmentMapper;
import com.clinic.appointmentsystem.domain.entities.Appointment;
import com.clinic.appointmentsystem.domain.entities.DoctorSchedule;
import com.clinic.appointmentsystem.domain.enums.AppointmentStatus;
import com.clinic.appointmentsystem.domain.enums.ShiftType;
import com.clinic.appointmentsystem.persistence.repositories.AppointmentRepository;
import com.clinic.appointmentsystem.persistence.repositories.DoctorScheduleRepository;
import com.clinic.appointmentsystem.persistence.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * AppointmentService - Randevu iş mantığı servisi
 * 
 * Bu servis randevu oluşturma, güncelleme, silme ve listeleme işlemlerini yönetir.
 * Randevu çakışmalarını kontrol eder ve doktor programlarına göre validasyon yapar.
 * Gelişmiş randevu süresi yönetimi ve müsait saat hesaplama özellikleri içerir.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    // Öğle arası zaman dilimi (12:00-13:00)
    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final AppointmentMapper mapper;
    private final DoctorScheduleRepository scheduleRepo;

    /**
     * Yeni randevu oluşturur
     * 
     * @param r Randevu oluşturma isteği
     * @return Oluşturulan randevunun ID'si
     * @throws IllegalArgumentException Geçersiz randevu zamanı veya çakışma durumunda
     */
    public UUID create(CreateAppointmentRequest r) {
        // Geçmiş tarih kontrolü
        if (r.appointmentTime().isBefore(LocalDateTime.now())) 
            throw new IllegalArgumentException("APPT_PAST_DATE");

        // Doktorun o gün çalışıp çalışmadığını kontrol et
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(r.doctorId(), r.appointmentTime().getDayOfWeek());
        if (schedule == null || !schedule.isWorkingDay()) 
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");

        // Randevu saatinin çalışma saatleri içinde olup olmadığını kontrol et
        LocalTime appointmentTime = r.appointmentTime().toLocalTime();
        if (appointmentTime.isBefore(schedule.getStartTime()) || appointmentTime.isAfter(schedule.getEndTime()))
            throw new IllegalArgumentException("APPT_OUTSIDE_WORKING_HOURS");

        // Randevu süresi kontrolü
        validateAppointmentTimeWithDuration(r.appointmentTime(), schedule);

        // Randevu çakışması kontrolü
        validateAppointmentConflict(r.doctorId(), r.appointmentTime(), schedule.getAppointmentDurationMinutes());

        // Hasta ve doktor bilgilerini al
        var patient = userRepo.findById(r.patientId()).orElseThrow(() -> new IllegalArgumentException("PATIENT_NOT_FOUND"));
        var doctor = userRepo.findById(r.doctorId()).orElseThrow(() -> new IllegalArgumentException("DOCTOR_NOT_FOUND"));

        // Yeni randevu oluştur
        var appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(r.appointmentTime())
                .status(AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repo.save(appointment);
        return appointment.getId();
    }

    /**
     * Hastanın randevularını listeler
     * 
     * @param patientId Hasta ID'si
     * @return Hasta randevularının listesi
     */
    @Transactional(readOnly = true)
    public List<AppointmentPatientView> findByPatient(UUID patientId) {
        return repo.findByPatientId(patientId).stream()
                .map(mapper::toPatientView)
                .toList();
    }

    /**
     * Doktorun randevularını listeler
     * 
     * @param doctorId Doktor ID'si
     * @return Doktor randevularının listesi
     */
    @Transactional(readOnly = true)
    public List<AppointmentDoctorView> findByDoctor(UUID doctorId) {
        return repo.findByDoctorId(doctorId).stream()
                .map(mapper::toDoctorView)
                .toList();
    }

    /**
     * Randevu durumunu günceller
     * 
     * @param id Randevu ID'si
     * @param status Yeni durum
     */
    public void updateStatus(UUID id, AppointmentStatus status) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        appt.setStatus(status);
        appt.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Randevuya not ekler
     * 
     * @param id Randevu ID'si
     * @param note Not içeriği
     */
    public void addNote(UUID id, String note) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        appt.setNote(note);
        appt.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Randevuyu yeniden planlar
     * 
     * @param id Randevu ID'si
     * @param newTime Yeni randevu zamanı
     */
    public void reschedule(UUID id, LocalDateTime newTime) {
        var appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("APPT_NOT_FOUND"));
        
        // Geçmiş tarih kontrolü
        if (newTime.isBefore(LocalDateTime.now())) 
            throw new IllegalArgumentException("APPT_PAST_DATE");

        // Doktorun o gün çalışıp çalışmadığını kontrol et
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(appt.getDoctor().getId(), newTime.getDayOfWeek());
        if (schedule == null || !schedule.isWorkingDay()) 
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");

        // Randevu saatinin çalışma saatleri içinde olup olmadığını kontrol et
        LocalTime appointmentTime = newTime.toLocalTime();
        if (appointmentTime.isBefore(schedule.getStartTime()) || appointmentTime.isAfter(schedule.getEndTime()))
            throw new IllegalArgumentException("APPT_OUTSIDE_WORKING_HOURS");

        // Randevu süresi kontrolü
        validateAppointmentTimeWithDuration(newTime, schedule);

        // Randevu çakışması kontrolü (kendi randevusu hariç)
        validateAppointmentConflictExcludingSelf(appt.getDoctor().getId(), newTime, schedule.getAppointmentDurationMinutes(), id);

        // Randevuyu güncelle
        appt.setAppointmentTime(newTime);
        appt.setUpdatedAt(LocalDateTime.now());
        appt.setStatus(AppointmentStatus.PENDING);
    }

    /**
     * Randevuyu siler
     * 
     * @param id Randevu ID'si
     */
    public void delete(UUID id) {
        if (!repo.existsById(id)) 
            throw new IllegalArgumentException("APPT_NOT_FOUND");
        repo.deleteById(id);
    }

    /**
     * Belirli bir doktorun belirli bir günde dolu olan zaman dilimlerini getirir
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih
     * @return Dolu zaman dilimlerinin listesi (HH:mm formatında)
     */
    @Transactional(readOnly = true)
    public List<String> getBookedTimeSlots(UUID doctorId, LocalDateTime date) {
        // Tarihi sistem saat dilimine çevir
        LocalDateTime localDate = date.atZone(java.time.ZoneOffset.UTC)
                .withZoneSameInstant(java.time.ZoneId.systemDefault())
                .toLocalDateTime();

        // Doktorun o günkü programını al
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctorId, localDate.getDayOfWeek());

        if (schedule == null || !schedule.isWorkingDay()) 
            throw new IllegalArgumentException("DOCTOR_NOT_WORKING");

        // Günün başlangıç ve bitiş saatlerini hesapla
        LocalDateTime startOfDay = localDate.toLocalDate().atTime(schedule.getStartTime());
        LocalDateTime endOfDay = localDate.toLocalDate().atTime(schedule.getEndTime());

        // O günkü randevuları al
        List<Appointment> appointments = repo.findByDoctorIdAndDateRange(doctorId, startOfDay, endOfDay);

        // Dolu zaman dilimlerini HH:mm formatında döndür
        return appointments.stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime().toString().substring(0, 5))
                .toList();
    }

    /**
     * Belirli bir doktorun belirli bir günde müsait olan zaman dilimlerini hesaplar
     * 
     * @param doctorId Doktor ID'si
     * @param date Tarih
     * @return Müsait zaman dilimlerinin listesi (HH:mm formatında)
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableTimeSlots(UUID doctorId, LocalDate date) {
        // Doktorun o günkü programını al
        DoctorSchedule schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());
        
        if (schedule == null || !schedule.isWorkingDay()) {
            return new ArrayList<>(); // Çalışma günü değil
        }

        // Dolu zaman dilimlerini al
        List<String> bookedSlots = getBookedTimeSlots(doctorId, date.atStartOfDay());
        
        // Tüm müsait zaman dilimlerini hesapla
        List<String> allAvailableSlots = calculateAllAvailableSlots(schedule);
        
        // Dolu olanları çıkar
        allAvailableSlots.removeAll(bookedSlots);
        
        return allAvailableSlots;
    }

    /**
     * Doktorun çalışma saatlerine göre tüm müsait zaman dilimlerini hesaplar
     * 
     * @param schedule Doktor programı
     * @return Tüm müsait zaman dilimleri
     */
    private List<String> calculateAllAvailableSlots(DoctorSchedule schedule) {
        List<String> availableSlots = new ArrayList<>();
        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int durationMinutes = schedule.getAppointmentDurationMinutes();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) || 
               currentTime.plusMinutes(durationMinutes).equals(endTime)) {
            
            // Tam gün vardiyasında öğle arası kontrolü
            if (schedule.getShiftType() == ShiftType.FULL_DAY) {
                LocalTime slotEndTime = currentTime.plusMinutes(durationMinutes);
                
                // Öğle arası ile çakışma kontrolü
                boolean conflictsWithLunch = (currentTime.isBefore(LUNCH_BREAK_END) && 
                                            slotEndTime.isAfter(LUNCH_BREAK_START));
                
                if (!conflictsWithLunch) {
                    availableSlots.add(currentTime.format(formatter));
                }
            } else {
                // Sabah veya öğleden sonra vardiyası için öğle arası kontrolü gerekmez
                availableSlots.add(currentTime.format(formatter));
            }
            
            // Bir sonraki zaman dilimine geç
            currentTime = currentTime.plusMinutes(durationMinutes);
        }

        return availableSlots;
    }

    /**
     * Randevu zamanını ve süresini kontrol eder
     * 
     * @param appointmentTime Randevu zamanı
     * @param schedule Doktor programı
     * @throws IllegalArgumentException Geçersiz randevu zamanı
     */
    private void validateAppointmentTimeWithDuration(LocalDateTime appointmentTime, DoctorSchedule schedule) {
        LocalTime appointmentStartTime = appointmentTime.toLocalTime();
        LocalTime appointmentEndTime = appointmentStartTime.plusMinutes(schedule.getAppointmentDurationMinutes());
        
        // Randevu bitiş saatinin çalışma saatleri içinde olup olmadığını kontrol et
        if (appointmentEndTime.isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException("APPT_EXCEEDS_WORKING_HOURS");
        }

        // Tam gün vardiyasında öğle arası kontrolü
        if (schedule.getShiftType() == ShiftType.FULL_DAY) {
            if ((appointmentStartTime.isBefore(LUNCH_BREAK_END) && appointmentEndTime.isAfter(LUNCH_BREAK_START))) {
                throw new IllegalArgumentException("APPT_DURING_LUNCH_BREAK");
            }
        }
    }

    /**
     * Randevu çakışması kontrolü
     * 
     * @param doctorId Doktor ID'si
     * @param appointmentTime Randevu zamanı
     * @param durationMinutes Randevu süresi
     * @throws IllegalStateException Çakışma durumunda
     */
    private void validateAppointmentConflict(UUID doctorId, LocalDateTime appointmentTime, int durationMinutes) {
        LocalDateTime appointmentEndTime = appointmentTime.plusMinutes(durationMinutes);
        
        // Aynı zaman diliminde başka randevu var mı kontrol et
        if (repo.existsByDoctorIdAndTimeRange(doctorId, appointmentTime, appointmentEndTime, durationMinutes)) {
            throw new IllegalStateException("APPT_TIME_SLOT_BOOKED");
        }
    }

    /**
     * Randevu çakışması kontrolü (belirli bir randevu hariç)
     * 
     * @param doctorId Doktor ID'si
     * @param appointmentTime Randevu zamanı
     * @param durationMinutes Randevu süresi
     * @param excludeAppointmentId Hariç tutulacak randevu ID'si
     * @throws IllegalStateException Çakışma durumunda
     */
    private void validateAppointmentConflictExcludingSelf(UUID doctorId, LocalDateTime appointmentTime, int durationMinutes, UUID excludeAppointmentId) {
        LocalDateTime appointmentEndTime = appointmentTime.plusMinutes(durationMinutes);
        
        // Aynı zaman diliminde başka randevu var mı kontrol et (kendi randevusu hariç)
        if (repo.existsByDoctorIdAndTimeRangeExcludingAppointment(doctorId, appointmentTime, appointmentEndTime, durationMinutes, excludeAppointmentId)) {
            throw new IllegalStateException("APPT_TIME_SLOT_BOOKED");
        }
    }
}
