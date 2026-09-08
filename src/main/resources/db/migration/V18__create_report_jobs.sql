CREATE TABLE report_jobs (
                             id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             business_id      UUID NOT NULL REFERENCES businesses (id),
                             requested_by     UUID NOT NULL REFERENCES users (id),
                             type             VARCHAR(30) NOT NULL,
                             start_date       DATE NOT NULL,
                             end_date         DATE NOT NULL,
                             status           VARCHAR(20) NOT NULL,
                             result_location  VARCHAR(500),
                             result_data      TEXT,
                             error_message    VARCHAR(1000),
                             created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                             started_at       TIMESTAMPTZ,
                             completed_at     TIMESTAMPTZ,
                             CONSTRAINT chk_report_jobs_type CHECK (type IN
                                                                    ('APPOINTMENTS','REVENUE','SERVICES','EMPLOYEES','CUSTOMERS','CANCELLATIONS','MONTHLY')),
                             CONSTRAINT chk_report_jobs_status CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED')),
                             CONSTRAINT chk_report_jobs_dates CHECK (start_date <= end_date)
);

CREATE INDEX idx_report_jobs_business ON report_jobs (business_id, created_at);
CREATE INDEX idx_report_jobs_status ON report_jobs (status);