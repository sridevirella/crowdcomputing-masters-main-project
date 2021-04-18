package com.app.service1MQ;

import com.app.service1MQ.model.MessagingChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.stream.annotation.EnableBinding;

/**
 * EnableEurekaClient enables microservice registration with Eureka server
 * EnableBinding enables microservice to bind the INPUT and OUTPUT channels with the RabbitMQ messaging queue.
 */
@EnableEurekaClient
@EnableBinding(MessagingChannel.class)
@SpringBootApplication
public class Service1MqApplication {

	public static void main(String[] args) {
		SpringApplication.run(Service1MqApplication.class, args);
	}

}
