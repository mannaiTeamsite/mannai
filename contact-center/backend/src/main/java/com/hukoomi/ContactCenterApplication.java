package com.hukoomi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages= {"com.hukoomi"})
public class ContactCenterApplication {
	private static final Logger logger = LoggerFactory.getLogger(ContactCenterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ContactCenterApplication.class, args);
	}

}
