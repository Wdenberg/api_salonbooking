CREATE TABLE employee_schedules (
                                    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    employee_id  UUID NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
                                    day_of_week  VARCHAR(10) NOT NULL,
                                    start_time   TIME NOT NULL,
                                    end_time     TIME NOT NULL,
                                    CONSTRAINT chk_employee_schedules_day CHECK (day_of_week IN
                                                                                 ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                                    CONSTRAINT chk_employee_schedules_range CHECK (start_time < end_time)
);

CREATE INDEX idx_employee_schedules_employee_day ON employee_schedules (employee_id, day_of_week);