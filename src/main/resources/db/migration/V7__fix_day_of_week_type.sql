-- Convert day_of_week from varchar to smallint
ALTER TABLE doctor_schedules
    ALTER COLUMN day_of_week TYPE smallint
    USING (
        CASE
            WHEN day_of_week = 'MONDAY' THEN 1
            WHEN day_of_week = 'TUESDAY' THEN 2
            WHEN day_of_week = 'WEDNESDAY' THEN 3
            WHEN day_of_week = 'THURSDAY' THEN 4
            WHEN day_of_week = 'FRIDAY' THEN 5
            WHEN day_of_week = 'SATURDAY' THEN 6
            WHEN day_of_week = 'SUNDAY' THEN 7
            ELSE NULL
        END
    ); 

DELETE FROM appointments; 