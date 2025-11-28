package com.iot.mobile_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iot.mobile_backend.service.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/command")
public class IoTDeviceController {

    private final MqttService mqttService;
    private static final Logger logger = LoggerFactory.getLogger(IoTDeviceController.class);

    @Autowired
    public IoTDeviceController(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    /*
    POST /api/command/heater/roomA?action=on
    POST /api/command/heater/roomA?action=off
    POST /api/command/fan/roomA?action=on
    POST /api/command/fan/roomA?action=off
    */
    @PostMapping("/{sensor}/{roomType}")
    public ResponseEntity<?> sendControlCommand(
            @PathVariable("sensor") String sensor,
            @PathVariable("roomType") String roomType,
            @RequestParam("action") String action) {

        logger.info("Received {} control request for room: {}...", sensor, roomType);
        logger.info("Turning {} the {}...", action, sensor);

        try {
            if (sensor == null || sensor.trim().isEmpty()) {
                logger.error("Sensor type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Sensor type is empty or null.");
            }
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (action == null || (!action.equalsIgnoreCase("off") && !action.equalsIgnoreCase("on"))) {
                logger.error("Action is invalid or null, aborting...");
                return ResponseEntity.badRequest().body("Action is invalid or null.");
            }

            // TODO: Execute publish command here...
            Boolean sensorStatus = action.equalsIgnoreCase("on");

            mqttService.publishSensorCommands(sensor, roomType, sensorStatus);

            logger.info("Command executed successfully in {}.", roomType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Command executed successfully.");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok().body(response);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid sensor type or action, aborting...");
            return ResponseEntity.badRequest().body("Invalid sensor type or action.");
        }
        catch (JsonProcessingException e) {
            logger.error("Error occurred while serializing JSON response: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
        catch (RuntimeException e) {
            logger.error("Error occurred while publishing the message: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
        catch (Exception e) {
            logger.error("Error occurred while processing request: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    // Example: POST /api/command/target-temp/roomA?temperature=74
    @PostMapping("/target-temp/{roomType}")
    public ResponseEntity<?> setTargetTemperature(@PathVariable("roomType") String roomType, @RequestParam("temperature") Double temperature) {
        logger.info("Setting target temperature for room: {} to {}...", roomType, temperature);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (temperature == null || temperature < 0) {
                logger.error("Temperature is invalid or null, aborting...");
                return ResponseEntity.badRequest().body("Temperature is invalid or null.");
            }
            if (temperature < 50 || temperature > 90) {
                logger.error("Temperature is out of range, aborting...");
                return ResponseEntity.badRequest().body("Temperature is out of range.");
            }

            // TODO: Execute publish command here...
            mqttService.publishTargetTemperature(roomType, temperature);
            logger.info("Target temperature set successfully in {}.", roomType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Target temperature set successfully.");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok().body(response);
        }
        catch (JsonProcessingException e) {
            logger.error("Error occurred while serializing JSON response: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
        catch (RuntimeException e) {
            logger.error("Error occurred while publishing the message: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
        catch (Exception e) {
            logger.error("Error occurred while processing request: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }
}
