package com.company.salonbooking.reporting.infrastructure.generator;

import com.company.salonbooking.reporting.application.port.ReportGenerator;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportType;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AppointmentsReportGenerator implements ReportGenerator {

    private final AppointmentRepository appointmentRepository;
    private final ObjectMapper objectMapper;

    public AppointmentsReportGenerator(AppointmentRepository appointmentRepository, ObjectMapper objectMapper) {
        this.appointmentRepository = appointmentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportType supports() {
        return ReportType.APPOINTMENTS;
    }

    @Override
    public String generate(ReportJob job) {
        var filter = new AppointmentFilter(null, null, null,
                job.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                job.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        List<Appointment> appointments = appointmentRepository.findByBusinessId(job.getBusinessId(), filter, 0, 10_000);

        List<Map<String, Object>> rows = appointments.stream().map(a -> Map.<String, Object>of(
                "id", a.getId().toString(),
                "status", a.getStatus().name(),
                "startAt", a.getStartAt().toString(),
                "serviceName", a.getServiceNameSnapshot(),
                "employeeName", a.getEmployeeNameSnapshot()
        )).collect(Collectors.toList());

        return writeJson(Map.of("reportType", "APPOINTMENTS", "totalCount", rows.size(), "appointments", rows));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize report result", e);
        }
    }
}
