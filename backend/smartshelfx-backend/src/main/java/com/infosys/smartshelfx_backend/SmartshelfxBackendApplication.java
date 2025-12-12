package com.infosys.smartshelfx_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

@SpringBootApplication
public class SmartshelfxBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartshelfxBackendApplication.class, args);
	}

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {
			try {
				flyway.repair();
			} catch (Exception ignored) {
			}
			flyway.migrate();
		};
	}

}
