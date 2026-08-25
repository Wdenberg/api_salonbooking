package com.company.salonbooking;


import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
public class SalonbookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalonbookingApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

}
