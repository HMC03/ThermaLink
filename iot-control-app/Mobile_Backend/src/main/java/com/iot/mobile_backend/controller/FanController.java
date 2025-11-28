package com.iot.mobile_backend.controller;

import com.iot.mobile_backend.dto.FanDTO;
import com.iot.mobile_backend.service.FanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fan")
public class FanController {

    private final FanService fanService;
    private static final Logger logger = LoggerFactory.getLogger(FanController.class);

    @Autowired
    public FanController(FanService fanService) {
        this.fanService = fanService;
    }

    // Example: GET /api/fan/status/roomA
    @GetMapping("/status/{roomType}")
    public ResponseEntity<?> getCurrentFanStatusByRoomType(@PathVariable("roomType") String roomType) {
        logger.info("Getting current fan status for room type: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }

            logger.info("Current Fan Status: {}", fanService.getCurrentFanStatusByRoomType(roomType));
            return ResponseEntity.ok(fanService.getCurrentFanStatusByRoomType(roomType));
        }
        catch (RuntimeException e) {
            logger.error("No fan status data found for this room: {}.", roomType);
            return ResponseEntity.notFound().build(); // 404
        }
        catch (Exception e) {
            logger.error("Error occurred while getting current fan status for room type: {}", roomType, e);
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    // Example: GET /api/fan/status/all
    @GetMapping("/status/all")
    public ResponseEntity<?> getAllFanStatusByRooms() {
        logger.info("Getting all fan status records...");

        try {
            return ResponseEntity.ok(fanService.getAllFanStatusRecordsByRoomType()); // Returns an empty list if no data found
        }
        catch (Exception e) {
            logger.error("Error occurred while getting all fan status records: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }

    // Example: POST /api/fan/create?roomType=roomA&fanStatus=true&recordingTime=2022-03-01T12:00:00Z
    @PostMapping("/create")
    public ResponseEntity<?> createNewFanStatusRecord(@Valid @RequestParam("roomtype") String roomType, @RequestParam("fanstatus") Boolean fanStatus, @RequestParam("recordingtime") String recordingTime ) {
        logger.info("Creating new fan status record for room: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (fanStatus == null) {
                logger.error("Person detected is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Person detected is empty or null.");
            }
            if (recordingTime == null || recordingTime.isEmpty()) {
                logger.error("Recording time is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Recording time is empty or null.");
            }

            FanDTO newFanStatus = new FanDTO();
            newFanStatus.setRoomType(roomType);
            newFanStatus.setFanStatus(fanStatus);
            newFanStatus.setRecordingTime(recordingTime);

            fanService.recordFanStatus(newFanStatus);
            return ResponseEntity.ok().body("Fan status record created successfully.");
        }
        catch (Exception e) {
            logger.error("Error occurred while creating new fan status record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }
}
