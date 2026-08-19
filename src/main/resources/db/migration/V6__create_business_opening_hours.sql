CREATE TABLE business_opening_hours (
                                        id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        business_id  UUID NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
                                        day_of_week  VARCHAR(10) NOT NULL,
                                        open_time    TIME NOT NULL,
                                        close_time   TIME NOT NULL,
                                        CONSTRAINT chk_opening_hours_day CHECK (day_of_week IN
                                                                                ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                                        CONSTRAINT chk_opening_hours_range CHECK (open_time < close_time)
);

CREATE INDEX idx_opening_hours_business_day ON business_opening_hours (business_id, day_of_week);
