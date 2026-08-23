package com.company.salonbooking.employee.interfaces.rest.dto;

import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(@Size(max = 150) String specialty) {}