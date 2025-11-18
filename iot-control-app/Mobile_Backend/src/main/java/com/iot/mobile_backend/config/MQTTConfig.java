package com.iot.mobile_backend.config;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@Configuration
public class MQTTConfig {

    @Value("${mqtt.brokerHost}")
    private String brokerUrl;

    @Value("${mqtt.brokerPort}")
    private Integer brokerPort;

    @Value("${mqtt.clientId}")
    private String clientId;

    private static final Logger logger = LoggerFactory.getLogger(MQTTConfig.class);

    @Bean
    public Mqtt5AsyncClient MQTTClient() {
        logger.info("Configuring MQTT client...");

        // Preconfigured MQTT client, connect to MQTT broker without needing to retype host and port.
        Mqtt5AsyncClient client = Mqtt5Client.builder()
                .identifier(clientId)
                .serverHost(brokerUrl)
                .serverPort(brokerPort)
                .sslWithDefaultConfig()
                .automaticReconnect()
                .initialDelay(1, TimeUnit.SECONDS)
                .maxDelay(10, TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .addConnectedListener(event -> logger.info("MQTT client connected at {}", LocalTime.now()))
                .addDisconnectedListener(event -> logger.info("MQTT client disconnected at {}", LocalTime.now()))
                .buildAsync();

        logger.info("MQTT client configured successfully");
        return client;
    }
}
