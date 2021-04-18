package com.app.service2DB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * EnableEurekaClient enables microservice registration with Eureka server
 * EnableBinding enables microservice to bind the INPUT and OUTPUT channels with the RabbitMQ messaging queue.
 */
@EnableEurekaClient
@SpringBootApplication
public class Service2DbApplication {

	public static void main(String[] args) {
		SpringApplication.run(Service2DbApplication.class, args);
	}

}
