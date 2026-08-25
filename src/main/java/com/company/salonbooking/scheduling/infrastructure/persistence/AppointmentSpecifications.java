package com.company.salonbooking.scheduling.infrastructure.persistence;

import com.company.salonbooking.scheduling.domain.repository.AppointmentFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Specification pattern (Seção 45) to avoid a repository method explosion for filter combinations. */
final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    static Specification<AppointmentJpaEntity> forCustomer(UUID customerId, AppointmentFilter filter) {
        return withFilter(filter).and((root, query, cb) -> cb.equal(root.get("customerId"), customerId));
    }

    static Specification<AppointmentJpaEntity> forBusiness(UUID businessId, AppointmentFilter filter) {
        return withFilter(filter).and((root, query, cb) -> cb.equal(root.get("businessId"), businessId));
    }

    private static Specification<AppointmentJpaEntity> withFilter(AppointmentFilter filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.employeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), filter.employeeId()));
            }
            if (filter.serviceId() != null) {
                predicates.add(cb.equal(root.get("serviceId"), filter.serviceId()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), filter.dateTo()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}