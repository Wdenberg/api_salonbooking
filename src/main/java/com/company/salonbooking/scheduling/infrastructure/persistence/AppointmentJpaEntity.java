package com.company.salonbooking.scheduling.infrastructure.persistence;

import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "service_name_snapshot", nullable = false, length = 150)
    private String serviceNameSnapshot;

    @Column(name = "service_price_amount_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal servicePriceAmountSnapshot;

    @Column(name = "service_price_currency_snapshot", nullable = false, length = 3)
    private String servicePriceCurrencySnapshot;

    @Column(name = "service_duration_minutes_snapshot", nullable = false)
    private int serviceDurationMinutesSnapshot;

    @Column(name = "employee_name_snapshot", nullable = false, length = 150)
    private String employeeNameSnapshot;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppointmentJpaEntity() {
    }

    public AppointmentJpaEntity(UUID id, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
                                Instant startAt, Instant endAt, AppointmentStatus status, String notes,
                                String serviceNameSnapshot, BigDecimal servicePriceAmountSnapshot,
                                String servicePriceCurrencySnapshot, int serviceDurationMinutesSnapshot,
                                String employeeNameSnapshot, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.businessId = businessId;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.serviceId = serviceId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.notes = notes;
        this.serviceNameSnapshot = serviceNameSnapshot;
        this.servicePriceAmountSnapshot = servicePriceAmountSnapshot;
        this.servicePriceCurrencySnapshot = servicePriceCurrencySnapshot;
        this.serviceDurationMinutesSnapshot = serviceDurationMinutesSnapshot;
        this.employeeNameSnapshot = employeeNameSnapshot;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getEmployeeId() { return employeeId; }
    public UUID getServiceId() { return serviceId; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getServiceNameSnapshot() { return serviceNameSnapshot; }
    public BigDecimal getServicePriceAmountSnapshot() { return servicePriceAmountSnapshot; }
    public String getServicePriceCurrencySnapshot() { return servicePriceCurrencySnapshot; }
    public int getServiceDurationMinutesSnapshot() { return serviceDurationMinutesSnapshot; }
    public String getEmployeeNameSnapshot() { return employeeNameSnapshot; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}