package com.company.salonbooking.reporting.infrastructure.generator;

import com.company.salonbooking.reporting.application.port.ReportGenerator;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportType;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import com.company.salonbooking.scheduling.domain.model.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Map;

@Component
public class CancellationsReportGenerator implements ReportGenerator {

    private final AppointmentRepository appointmentRepository;
    private final ObjectMapper objectMapper;

    public CancellationsReportGenerator(AppointmentRepository appointmentRepository, ObjectMapper objectMapper) {
        this.appointmentRepository = appointmentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportType supports() {
        return ReportType.CANCELLATIONS;
    }

    @Override
    public String generate(ReportJob job) {
        var filter = new AppointmentFilter(AppointmentStatus.CANCELLED, null, null,
                job.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                job.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        int count = appointmentRepository.findByBusinessId(job.getBusinessId(), filter, 0, 10_000).size();

        return writeJson(Map.of("reportType", "CANCELLATIONS", "cancelledCount", count));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize report result", e);
        }
    }
}
