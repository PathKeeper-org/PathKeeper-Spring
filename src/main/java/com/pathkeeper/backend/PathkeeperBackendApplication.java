package com.pathkeeper.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PathkeeperBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PathkeeperBackendApplication.class, args);
	}

}
