package com.app.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Enables eureka service registry on port 8671
 * Holds information about all other the micro services
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

	public static void main(String[] args) {

		SpringApplication.run(EurekaServerApplication.class, args);
		System.out.println("Eureka Server Started...");
	}
}
