package com.proyecto.ecommerce_minerva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@EntityScan(basePackages = { "com.proyecto.model" })
@ComponentScan(basePackages = {
		"com.proyecto.ecommerce_minerva",
		"com.proyecto.config",
		"com.proyecto.Controller",
		"com.proyecto.service",
		"com.proyecto.repositories",
		"com.proyecto.request",
		"com.proyecto.response",
		"com.proyecto.Exception",
		"com.proyecto.security",
		"com.proyecto.scheduler"
})
@EnableJpaRepositories(basePackages = { "com.proyecto.repositories" })
public class EcommerceMinervaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceMinervaApplication.class, args);
	}

}
