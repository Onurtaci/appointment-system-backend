-- Add a function to set shift times based on shift_type
CREATE OR REPLACE FUNCTION set_shift_times()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.shift_type = 'MORNING' THEN
        NEW.start_time := '09:00';
        NEW.end_time := '12:00';
    ELSIF NEW.shift_type = 'AFTERNOON' THEN
        NEW.start_time := '13:00';
        NEW.end_time := '18:00';
    ELSIF NEW.shift_type = 'FULL_DAY' THEN
        NEW.start_time := '09:00';
        NEW.end_time := '18:00';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER set_shift_times_trigger
BEFORE INSERT OR UPDATE ON doctor_schedules
FOR EACH ROW
EXECUTE FUNCTION set_shift_times(); 