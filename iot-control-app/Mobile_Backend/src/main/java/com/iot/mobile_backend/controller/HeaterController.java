package com.iot.mobile_backend.controller;

import com.iot.mobile_backend.dto.HeaterDTO;
import com.iot.mobile_backend.service.HeaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/heater")
public class HeaterController {

    private final HeaterService heaterService;
    private static final Logger logger = LoggerFactory.getLogger(HeaterController.class);

    @Autowired
    public HeaterController(HeaterService heaterService) {
        this.heaterService = heaterService;
    }

    @GetMapping("/status/{roomType}")
    public ResponseEntity<?> getCurrentHeaterStatusByRoomType(@PathVariable("roomType") String roomType) {
        logger.info("Getting current heater status for room type: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }

            logger.info("Current Heater Status: {}", heaterService.getCurrentHeaterStatusByRoomType(roomType));
            return ResponseEntity.ok(heaterService.getCurrentHeaterStatusByRoomType(roomType));
        }
        catch (RuntimeException e) {
            logger.error("No heater status data found for this room: {}.", roomType);
            return ResponseEntity.notFound().build(); // 404
        }
        catch (Exception e) {
            logger.error("Error occurred while getting current heater status for room type: {}", roomType, e);
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    @GetMapping("/status/all")
    public ResponseEntity<?> getAllHeaterStatusByRoom () {
        logger.info("Getting all latest heater status records...");

        try {
            return ResponseEntity.ok(heaterService.getAllHeaterStatusRecordsByRoomType());
        }
        catch (Exception e) {
            logger.error("Error occurred while getting all heater status records: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createHeaterRecord(@RequestParam("roomtype") String roomType, @RequestParam("heaterstatus") Boolean heaterStatus, @RequestParam("recordingtime") String recordingTime) {
        logger.info("Creating new heater record for room: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (heaterStatus == null) {
                logger.error("Person detected is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Person detected is empty or null.");
            }
            if (recordingTime == null || recordingTime.isEmpty()) {
                logger.error("Recording time is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Recording time is empty or null.");
            }

            HeaterDTO newheaterStatus = new HeaterDTO();
            newheaterStatus.setRoomType(roomType);
            newheaterStatus.setHeaterStatus(heaterStatus);
            newheaterStatus.setRecordingTime(recordingTime);

            heaterService.recordHeaterStatus(newheaterStatus);
            return ResponseEntity.ok().body("Heater status record created successfully.");
        }
        catch (Exception e) {
            logger.error("Error occurred while creating new heater status record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }
}
