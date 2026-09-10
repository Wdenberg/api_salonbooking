package com.company.salonbooking.reporting.infrastructure.generator;

import com.company.salonbooking.reporting.application.port.ReportGenerator;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportType;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import com.company.salonbooking.scheduling.domain.model.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Revenue is computed strictly from COMPLETED appointments using their historical
 * price snapshot (Seção 130) — never re-reading the current ServiceOffering price,
 * so past reports remain correct even after later price changes.
 */
@Component
public class RevenueReportGenerator implements ReportGenerator {

    private final AppointmentRepository appointmentRepository;
    private final ObjectMapper objectMapper;

    public RevenueReportGenerator(AppointmentRepository appointmentRepository, ObjectMapper objectMapper) {
        this.appointmentRepository = appointmentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportType supports() {
        return ReportType.REVENUE;
    }

    @Override
    public String generate(ReportJob job) {
        var filter = new AppointmentFilter(AppointmentStatus.COMPLETED, null, null,
                job.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                job.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        List<Appointment> completed = appointmentRepository.findByBusinessId(job.getBusinessId(), filter, 0, 10_000);

        BigDecimal total = completed.stream()
                .map(a -> a.getServicePriceSnapshot().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = completed.isEmpty() ? "BRL" : completed.get(0).getServicePriceSnapshot().getCurrencyCode();

        return writeJson(Map.of(
                "reportType", "REVENUE",
                "completedAppointments", completed.size(),
                "totalRevenue", total,
                "currency", currency
        ));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize report result", e);
        }
    }
}