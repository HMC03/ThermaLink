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
        System.setProperty("MQTT_BROKER_HOST", dotenv.get("MQTT_BROKER_HOST"));
        System.setProperty("MQTT_BROKER_PORT", dotenv.get("MQTT_BROKER_PORT"));
        System.setProperty("MQTT_USERNAME", dotenv.get("MQTT_USERNAME"));
        System.setProperty("MQTT_PASSWORD", dotenv.get("MQTT_PASSWORD"));
        System.setProperty("MQTT_CLIENT_ID", dotenv.get("MQTT_CLIENT_ID"));
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
