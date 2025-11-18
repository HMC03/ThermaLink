package com.iot.mobile_backend.service;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class MqttService {

    private final Mqtt5AsyncClient mqttClient;
    private final TemperatureService temperatureService;
    private final PersonDetectService personDetectService;
    private final static Logger logger = LoggerFactory.getLogger(MqttService.class);
    private final ApplicationContext applicationContext;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Autowired
    public MqttService(Mqtt5AsyncClient mqttClient, TemperatureService temperatureService, PersonDetectService personDetectService, ApplicationContext applicationContext) {
        this.mqttClient = mqttClient;
        this.temperatureService = temperatureService;
        this.personDetectService = personDetectService;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void connectAndSubscribe() {
        logger.info("Initializing MQTT service...");

        try {
            connectToMqtt()
                    .thenCompose(connAck -> subscribeToTopics())
                    .get(5, TimeUnit.SECONDS);

            logger.info("MQTT service initialized successfully");
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            logger.error("MQTT connection failed: {}", cause.getMessage(), cause);

            if (cause.getMessage().contains("authentication") ||
                    cause.getMessage().contains("Not authorized") ||
                    cause.getMessage().contains("Bad user name or password")) {
                logger.error("MQTT Authentication failed - check username/password");
            }

            // Shutdown application
            SpringApplication.exit(applicationContext, () -> 1);
            System.exit(1);
        }
        catch (TimeoutException e) {
            logger.error("MQTT connection timed out (likely authentication failure)");
            logger.error("Check: 1) Broker host/port 2) Username/password 3) Network connectivity");

            // Shutdown application
            SpringApplication.exit(applicationContext, () -> 1);
            System.exit(1);
        }
        catch (Exception e) {
            logger.error("MQTT initialization failed", e);

            // Shutdown application
            SpringApplication.exit(applicationContext, () -> 1);
            System.exit(1);
        }
    }

    private CompletableFuture<Mqtt5ConnAck> connectToMqtt() {
        logger.info("Connecting to MQTT broker...");

        // Authorize credentials to connect to the broker
        return mqttClient.connectWith()
                .simpleAuth()
                    .username(username)
                    .password(UTF_8.encode(password))
                    .applySimpleAuth()
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to authorize credentials: {}.", throwable.getMessage() ,throwable);
                    }
                    else {
                        logger.info("MQTT client connected successfully.");
                    }
                });
    }

    private CompletableFuture<Void> subscribeToTopics() {
        logger.info("Subscribing to all topics...");

        CompletableFuture<Void> recieveBaseTempSub = mqttClient.subscribeWith()
                .topicFilter("temperature/status/base")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleTemperatureMessage)
                .send()
                .thenRun(() -> logger.info("Subscribed to topic: temperature/status/base"));

        CompletableFuture<Void> recieveHeaterTempSub = mqttClient.subscribeWith()
                .topicFilter("temperature/status/heater")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleTemperatureMessage)
                .send()
                .thenRun(() -> logger.info("Subscribed to topic: temperature/status/heater"));

        CompletableFuture<Void> recieveCameraDetectionSub = mqttClient.subscribeWith()
                .topicFilter("camera/status")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handlePersonDetectionMessage)
                .send()
                .thenRun(() -> logger.info("Subscribed to topic: camera/status"));

        // Organize all subscriptions here, so we can subscribe to all topics at once
        return CompletableFuture.allOf(recieveBaseTempSub, recieveHeaterTempSub, recieveCameraDetectionSub)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to subscribe to topics.", throwable);
                    }
                    else {
                        logger.info("Successfully subscribed to all topics.");
                    }
                });
    }

    private void handleTemperatureMessage(Mqtt5Publish message) {
        try {
            String payload = new String(message.getPayloadAsBytes(), UTF_8);
            logger.info("Received temperature reading: {}", payload);

            // TODO: Save temperature reading to database
        }
        catch (Exception e) {
            logger.error("Error occurred while handling temperature message.", e);
            logger.error("Nothing has been done with the message.", e);
        }
    }

    private void handlePersonDetectionMessage(Mqtt5Publish message) {
        try {
            String payload = new String(message.getPayloadAsBytes(), UTF_8);
            logger.info("Received person detection status: {}", payload);

            // TODO: Save person detection status to database
        }
        catch (Exception e) {
            logger.error("Error occurred while handling person detection message.", e);
            logger.error("Nothing has been done with the message.", e);
        }
    }
}
