-- Add appointment duration column to doctor_schedules
ALTER TABLE doctor_schedules ADD appointment_duration_minutes INTEGER NOT NULL DEFAULT 30;

-- Add shift type column to doctor_schedules
ALTER TABLE doctor_schedules ADD shift_type VARCHAR(10) NOT NULL DEFAULT 'FULL_DAY';
ALTER TABLE doctor_schedules ADD CONSTRAINT check_shift_type CHECK (shift_type IN ('MORNING', 'AFTERNOON', 'FULL_DAY')); 