package com.iot.mobile_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CORSConfig {

    // https://www.geeksforgeeks.org/spring-security-cors-configuration/
    // https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html

    private static final Logger logger = LoggerFactory.getLogger(CORSConfig.class);

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String allowedOrigin = System.getenv("CORS_ALLOWED_ORIGIN");

        if (allowedOrigin == null || allowedOrigin.isBlank()) {
            logger.warn("Default CORS allowed origin set to \"*\"");
            allowedOrigin = "*";
        }

        logger.info("Configuring CORS setting");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false); // Development purpose
        config.addAllowedOrigin(allowedOrigin);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        logger.info("CORS configured to allow origin at {}", allowedOrigin);
        return source;
    }
}
