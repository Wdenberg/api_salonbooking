package com.company.salonbooking.identity.application.command;

public record RegisterOwnerCommand(String name, String email, String rawPassword) {
}
