package com.company.salonbooking.business.domain.model;

public record Address(String street, String number, String city, String state, String zipCode, String country) {

    public static Address empty(){
        return new Address(null, null, null, null, null, null);
    }

}
