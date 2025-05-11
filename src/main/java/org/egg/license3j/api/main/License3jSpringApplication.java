package org.egg.license3j.api.main;

import org.egg.license3j.api.controllers.LicenseController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = LicenseController.class)
public class License3jSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(License3jSpringApplication.class, args);
	}
}
