package com.app.service4re;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * EnableEurekaClient enables microservice registration with Eureka server
 * EnableBinding enables microservice to bind the INPUT and OUTPUT channels with the RabbitMQ messaging queue.
 */
@EnableEurekaClient
@SpringBootApplication
public class Service4ReApplication {

	public static void main(String[] args) {
		SpringApplication.run(Service4ReApplication.class, args);
	}

}
