package com.iot.mobile_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.iot.mobile_backend.dto.PersonDetectionDTO;
import com.iot.mobile_backend.dto.TemperatureDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
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
    private void connectAndSubscribe() {
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

    @PreDestroy
    private void disconnect() {
        logger.info("Shutting down connection to MQTT broker...");

        try {
            if (mqttClient.getState().isConnected()) {
                logger.info("Disconnecting from MQTT broker...");

                mqttClient.disconnect()
                        .get(3, TimeUnit.SECONDS);

                logger.info("Disconnected from MQTT broker successfully.");
            }
            else {
                logger.info("MQTT broker is already disconnected.");
            }
        }
        catch (TimeoutException e) {
            logger.error("MQTT broker disconnect timed out.");
        }
        catch (Exception e) {
            logger.error("Error occurred while disconnecting from MQTT broker.", e);
        }

        logger.info("MQTT connection shut down successfully.");
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

//        CompletableFuture<Void> recieveBaseTempSub = mqttClient.subscribeWith()
//                .topicFilter("temperature/status/base")
//                .qos(MqttQos.AT_LEAST_ONCE)
//                .callback(this::handleTemperatureMessage)
//                .send()
//                .thenRun(() -> logger.info("Subscribed to topic: temperature/status/base"));

        CompletableFuture<Void> recieveAllRoomTempSub = mqttClient.subscribeWith()
                .topicFilter("+/temperature/status")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleTemperatureMessage)
                .send()
                .thenRun(() -> logger.info("Subscribed to all temperature topics"));

//        CompletableFuture<Void> recieveHeaterTempSub = mqttClient.subscribeWith()
//                .topicFilter("roomA/temperature/status")
//                .qos(MqttQos.AT_LEAST_ONCE)
//                .callback(this::handleTemperatureMessage)
//                .send()
//                .thenRun(() -> logger.info("Subscribed to topic: temperature/status/heater"));
//
//        CompletableFuture<Void> recieveCameraDetectionSub = mqttClient.subscribeWith()
//                .topicFilter("roomA/person/status")
//                .qos(MqttQos.AT_LEAST_ONCE)
//                .callback(this::handlePersonDetectionMessage)
//                .send()
//                .thenRun(() -> logger.info("Subscribed to topic: camera/status"));

        CompletableFuture<Void> recieveAllPersonRoomDetectionSub = mqttClient.subscribeWith()
                .topicFilter("+/person/status")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handlePersonDetectionMessage)
                .send()
                .thenRun(() -> logger.info("Subscribed to all person detection topics"));

        // Organize all subscriptions here, so we can subscribe to all topics at once
        return CompletableFuture.allOf(recieveAllRoomTempSub, recieveAllPersonRoomDetectionSub)
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
            logger.info("Received temperature reading: {}.", payload);

            // Example String format: {"temp_f": 74.1, "timestamp": "2025-11-24T19:36:55"}
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode data = objectMapper.readTree(payload); // Convert string to JSON

            String topic = message.getTopic().toString();
            String roomType = extractRoomTypeFromTopic(topic);
            logger.info("Room type: {}.", roomType);

            Double temperature = data.get("temp_f").asDouble();
            String recordingTime = data.get("timestamp").asText();

            if (roomType.isEmpty()) {
                logger.error("Invalid inputs, aborting...");
                throw new InputMismatchException("Invalid inputs, aborting...");
            }
            // Note: Could be a better way to check invalid temperature, but not sure.
            if (temperature < 0) {
                logger.error("Invalid inputs, aborting...");
                throw new InputMismatchException("Invalid inputs, aborting...");
            }
            if (recordingTime.isEmpty()) {
                logger.error("Invalid inputs, aborting...");
                throw new InputMismatchException("Invalid inputs, aborting...");
            }

            TemperatureDTO newTemperature = new TemperatureDTO();
            newTemperature.setRoomType(roomType);
            newTemperature.setTemperature(temperature);
            newTemperature.setRecordingTime(recordingTime);

            temperatureService.recordTemperature(newTemperature);
            logger.info("Added temperature: {}°F for the room, {}.", temperature, roomType);
        }
        catch (Exception e) {
            logger.error("Error occurred while handling temperature message.");
            logger.error("Nothing has been done with the message.", e);
        }
    }

    private void handlePersonDetectionMessage(Mqtt5Publish message) {
        try {
            String payload = new String(message.getPayloadAsBytes(), UTF_8);
            logger.info("Received person detection status: {}.", payload);

            // Example String format: {"status": true, "confidence": 0.8003, "timestamp": "2025-11-24T19:36:56"}
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode data = objectMapper.readTree(payload); // Convert string to JSON

            boolean personDetected = data.get("status").asBoolean();
            double confidence = data.get("confidence").asDouble();
            String detectionTime = data.get("timestamp").asText();

            if (confidence < 0 || confidence > 1) {
                logger.error("Invalid confidence value, aborting...");
                throw new InputMismatchException("Invalid confidence value, aborting...");
            }
            if (detectionTime.isEmpty()) {
                logger.error("Invalid inputs, aborting...");
                throw new InputMismatchException("Invalid inputs, aborting...");
            }

            PersonDetectionDTO newDetection = new PersonDetectionDTO();
            newDetection.setPersonDetected(personDetected);
            newDetection.setConfidence(confidence);
            newDetection.setDetectionTime(detectionTime);

            personDetectService.recordPersonDetection(newDetection);

            if (personDetected && confidence >= 0.65) {
                logger.info("Person entered the room at: {}.", detectionTime);
            }
            else {
                logger.info("Room is empty.");
            }
        }
        catch (Exception e) {
            logger.error("Error occurred while handling person detection message.", e);
            logger.error("Nothing has been done with the message.", e);
        }
    }

    private String extractRoomTypeFromTopic(String topic) {
        String[] topicByParts = topic.split("/");

        if (topicByParts.length < 2) {
            logger.error("Topic name only contains one part. Returning topic name...");
            return topic;
        }

        // Assuming all topic names start with a room type.
        return topicByParts[0];
    }
}
