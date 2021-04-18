package com.app.service3te;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * EnableEurekaClient enables microservice registration with Eureka server
 * EnableBinding enables microservice to bind the INPUT and OUTPUT channels with the RabbitMQ messaging queue.
 */
@EnableEurekaClient
@SpringBootApplication
public class Service3TeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Service3TeApplication.class, args);
	}

}
