package com.iot.mobile_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MobileBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(MobileBackendApplication.class);

    public static void main(String[] args) {
        logger.info("Configuring environment variables...");
        Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("CORS_ALLOWED_ORIGIN", dotenv.get("CORS_ALLOWED_ORIGIN"));
        logger.info("Environment variables configured successfully");

        logger.info("Starting Spring Boot application...");
        SpringApplication.run(MobileBackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application is ready to accept API requests.");
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        logger.info("Application is shutting down...");
    }
}
