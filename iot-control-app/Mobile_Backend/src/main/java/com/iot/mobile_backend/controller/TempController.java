package com.iot.mobile_backend.controller;

import com.iot.mobile_backend.dto.TemperatureDTO;
import com.iot.mobile_backend.model.TemperatureSensor;
import com.iot.mobile_backend.service.TemperatureService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temperature")
public class TempController {

    private final TemperatureService temperatureService;
    private static final Logger logger = LoggerFactory.getLogger(TempController.class);

    @Autowired
    public TempController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/status/{roomType}")
    public ResponseEntity<?> getCurrentTemperatureByRoomType(@PathVariable("roomType") String roomType) {
        logger.info("Getting current temperature for room type: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.warn("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is required.");
            }

            TemperatureSensor currentTemp = temperatureService.getCurrentTemperatureByRoomType(roomType);
            return ResponseEntity.ok(currentTemp);
        }
        catch (RuntimeException e) {
            logger.error("No temperature data found for room type: {}", roomType, e);
            return ResponseEntity.notFound().build();  // 404
        }
        catch (Exception e) {
            logger.error("Error occurred while getting current temperature for room type: {}", roomType, e);
            return ResponseEntity.internalServerError().body("Internal server error.");  // 500
        }
    }

    @GetMapping("/status/all")
    public ResponseEntity<List<TemperatureSensor>> getAllRoomTemperatures() {
        logger.info("Getting all room temperatures...");

        try {
            List<TemperatureSensor> temperatures = temperatureService.getAllRoomTemperatures();
            return ResponseEntity.ok(temperatures);  // Will return empty list if no data
        }
        catch (Exception e) {
            logger.error("Error occurred while getting all room temperatures", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTemperatureRecord(@Valid @RequestParam("roomtype") String roomType, @RequestParam("temperature") Double temperature, @RequestParam("recordingtime") String recordingTime) {
        logger.info("Creating new temperature record for room: {}...", roomType);

        try {
            if (roomType.isEmpty()) {
                logger.warn("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (temperature == null || temperature < 0) {
                logger.warn("Temperature is invalid, aborting...");
                return ResponseEntity.badRequest().body("Temperature is invalid.");
            }
            if (recordingTime.isEmpty()) {
                logger.warn("Recording time is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Recording time is empty or null.");
            }

            TemperatureDTO temperatureDTO = new TemperatureDTO();
            temperatureDTO.setRoomType(roomType);
            temperatureDTO.setTemperature(temperature);
            temperatureDTO.setRecordingTime(recordingTime);

            temperatureService.recordTemperature(temperatureDTO);

            return ResponseEntity.ok("Temperature record created successfully.");
        }
        catch (Exception e) {
            logger.error("Error occurred while creating new temperature record for room: {}", roomType, e);
            return ResponseEntity.internalServerError().body("Error occurred while creating new temperature record for a room.");   // 500
        }
    }
}
