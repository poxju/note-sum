package com.poxju.proksi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProksiApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProksiApplication.class);

	public static void main(String[] args) {
		// Debug environment variables at startup
		logger.info("=== ENVIRONMENT VARIABLES DEBUG ===");
		logger.info("DATABASE_URL: {}", System.getenv("DATABASE_URL") != null ? "SET" : "NOT_SET");
		logger.info("PGUSER: {}", System.getenv("PGUSER") != null ? "SET" : "NOT_SET");
		logger.info("PGPASSWORD: {}", System.getenv("PGPASSWORD") != null ? "SET" : "NOT_SET");
		logger.info("JWT_SECRET: {}", System.getenv("JWT_SECRET") != null ? "SET" : "NOT_SET");
		logger.info("HUGGINGFACE_API_TOKEN: {}", System.getenv("HUGGINGFACE_API_TOKEN") != null ? "SET" : "NOT_SET");
		logger.info("=== END DEBUG ===");
		
		SpringApplication.run(ProksiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		logger.info("Application started successfully!");
	}
}
 