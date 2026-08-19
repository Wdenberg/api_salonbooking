package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.BusinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "businesses")
public class BusinessJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "address_street", length = 200)
    private String addressStreet;

    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_zip_code", length = 20)
    private String addressZipCode;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BusinessStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BusinessJpaEntity() {
    }

    public BusinessJpaEntity(UUID id, UUID ownerId, String name, String description, String phone, String email,
                             String addressStreet, String addressNumber, String addressCity, String addressState,
                             String addressZipCode, String addressCountry, String timezone, BusinessStatus status,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressZipCode = addressZipCode;
        this.addressCountry = addressCountry;
        this.timezone = timezone;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddressStreet() { return addressStreet; }
    public String getAddressNumber() { return addressNumber; }
    public String getAddressCity() { return addressCity; }
    public String getAddressState() { return addressState; }
    public String getAddressZipCode() { return addressZipCode; }
    public String getAddressCountry() { return addressCountry; }
    public String getTimezone() { return timezone; }
    public BusinessStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}