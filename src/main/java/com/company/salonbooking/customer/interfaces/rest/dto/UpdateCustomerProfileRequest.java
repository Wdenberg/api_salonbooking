package com.company.salonbooking.customer.interfaces.rest.dto;

import java.time.LocalDate;

public record UpdateCustomerProfileRequest(String phone, LocalDate dateOfBirth) {}