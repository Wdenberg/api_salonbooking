package com.company.salonbooking.identity.application.command;

public record RegisterCustomerCommand(String name, String email, String rawPassword) {
}
