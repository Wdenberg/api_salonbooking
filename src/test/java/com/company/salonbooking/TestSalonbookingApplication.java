package com.company.salonbooking;

import org.springframework.boot.SpringApplication;

public class TestSalonbookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(SalonbookingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
