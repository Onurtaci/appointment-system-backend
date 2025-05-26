-- Update existing schedules to respect lunch break
UPDATE doctor_schedules 
SET start_time = '09:00', end_time = '12:00'
WHERE shift_type = 'MORNING';

UPDATE doctor_schedules 
SET start_time = '13:00', end_time = '18:00'
WHERE shift_type = 'AFTERNOON';

-- For full day shifts, ensure they don't overlap with lunch break
UPDATE doctor_schedules 
SET start_time = '09:00', end_time = '18:00'
WHERE shift_type = 'FULL_DAY'
AND ((start_time <= '12:00' AND end_time >= '12:00')
  OR (start_time <= '13:00' AND end_time >= '13:00')
  OR (start_time >= '12:00' AND end_time <= '13:00'));

-- Add constraint to ensure morning shifts end before lunch
ALTER TABLE doctor_schedules 
ADD CONSTRAINT check_morning_shift 
CHECK (shift_type != 'MORNING' OR end_time <= '12:00');

-- Add constraint to ensure afternoon shifts start after lunch
ALTER TABLE doctor_schedules 
ADD CONSTRAINT check_afternoon_shift 
CHECK (shift_type != 'AFTERNOON' OR start_time >= '13:00');

-- Add constraint to ensure full day shifts respect lunch break
ALTER TABLE doctor_schedules 
ADD CONSTRAINT check_full_day_shift 
CHECK (shift_type != 'FULL_DAY' 
    OR (start_time <= '12:00' AND end_time >= '13:00' 
        AND NOT (start_time > '12:00' AND start_time < '13:00')
        AND NOT (end_time > '12:00' AND end_time < '13:00'))); 