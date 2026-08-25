package com.company.salonbooking.scheduling.infrastructure.persistence;

import com.company.salonbooking.scheduling.domain.exception.AppointmentConflictException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.domain.model.Money;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Appointment> findActiveByEmployeeAndRange(UUID employeeId, Instant from, Instant to) {
        return jpaRepository.findActiveByEmployeeAndRange(employeeId, from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsOverlapping(UUID employeeId, Instant startAt, Instant endAt) {
        return jpaRepository.existsOverlapping(employeeId, startAt, endAt);
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentJpaEntity entity = toEntity(appointment);
        try {
            AppointmentJpaEntity saved = jpaRepository.saveAndFlush(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            // Translates the PostgreSQL exclusion_violation (23P01) raised by
            // excl_appointments_employee_overlap into a domain-level conflict (Seção 24, step 3).
            throw new AppointmentConflictException();
        }
    }

    @Override
    public List<Appointment> findByCustomerId(UUID customerId, AppointmentFilter filter, int page, int size) {
        var spec = AppointmentSpecifications.forCustomer(customerId, filter);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
        return jpaRepository.findAll(spec, pageable).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByBusinessId(UUID businessId, AppointmentFilter filter, int page, int size) {
        var spec = AppointmentSpecifications.forBusiness(businessId, filter);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
        return jpaRepository.findAll(spec, pageable).stream().map(this::toDomain).toList();
    }

    private AppointmentJpaEntity toEntity(Appointment a) {
        return new AppointmentJpaEntity(a.getId(), a.getBusinessId(), a.getCustomerId(), a.getEmployeeId(), a.getServiceId(),
                a.getStartAt(), a.getEndAt(), a.getStatus(), a.getNotes(), a.getServiceNameSnapshot(),
                a.getServicePriceSnapshot().getAmount(), a.getServicePriceSnapshot().getCurrencyCode(),
                a.getServiceDurationMinutesSnapshot(), a.getEmployeeNameSnapshot(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private Appointment toDomain(AppointmentJpaEntity e) {
        return Appointment.restore(e.getId(), e.getBusinessId(), e.getCustomerId(), e.getEmployeeId(), e.getServiceId(),
                e.getStartAt(), e.getEndAt(), e.getStatus(), e.getNotes(), e.getServiceNameSnapshot(),
                Money.of(e.getServicePriceAmountSnapshot(), e.getServicePriceCurrencySnapshot()),
                e.getServiceDurationMinutesSnapshot(), e.getEmployeeNameSnapshot(), e.getCreatedAt(), e.getUpdatedAt());
    }
}