package com.company.salonbooking.customer.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateCustomerProfileCommand(UUID userId, String phone, LocalDate dateOfBirth) {}