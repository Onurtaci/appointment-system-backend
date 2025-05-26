-- Allow null values for start_time and end_time
ALTER TABLE doctor_schedules ALTER COLUMN start_time DROP NOT NULL;
ALTER TABLE doctor_schedules ALTER COLUMN end_time DROP NOT NULL;

-- Update any existing records that might have null times
UPDATE doctor_schedules 
SET start_time = '09:00'::TIME, end_time = '12:00'::TIME
WHERE shift_type = 'MORNING' AND (start_time IS NULL OR end_time IS NULL);

UPDATE doctor_schedules 
SET start_time = '13:00'::TIME, end_time = '18:00'::TIME
WHERE shift_type = 'AFTERNOON' AND (start_time IS NULL OR end_time IS NULL);

UPDATE doctor_schedules 
SET start_time = '09:00'::TIME, end_time = '18:00'::TIME
WHERE shift_type = 'FULL_DAY' AND (start_time IS NULL OR end_time IS NULL); 