package com.company.salonbooking.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerProfileJpaRepository extends JpaRepository<CustomerProfileJpaEntity, UUID> {
}