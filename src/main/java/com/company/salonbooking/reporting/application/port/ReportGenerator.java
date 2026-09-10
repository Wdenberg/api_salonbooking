package com.company.salonbooking.reporting.application.port;

import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportType;

/**
 * Strategy per report type (Seção 35's ReportType enum). Each implementation reads
 * whatever data it needs directly from the relevant module's repositories and produces
 * the report body as a JSON string — kept generic rather than typed, since different
 * report types have entirely different shapes and there is no shared consumer of the
 * structured result yet (Seção 148, YAGNI).
 */
public interface ReportGenerator {

    ReportType supports();

    String generate(ReportJob job);
}
