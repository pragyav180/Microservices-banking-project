package com.discovery.agent.Discovery_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryAgentApplication.class, args);
	}

}
