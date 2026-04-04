package com.accounts.project;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef="auditAwareImpl")
@OpenAPIDefinition(
		info = @Info(
				title="This is an Accounts microservice built by Pragya",
				description="This is an Accounts microservice that has major rntities as Accounts and customer",
				version = "v1",
				contact = @Contact(
						name="Pragya Verma",
						email="pragyav180@gmail.com"
				),
				license = @License(
						name = "apache 2.0"
				)
		)
)
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}
