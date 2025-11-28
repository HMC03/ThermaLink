package com.iot.mobile_backend.controller;

import com.iot.mobile_backend.dto.PersonDetectionDTO;
import com.iot.mobile_backend.service.PersonDetectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person-detect")
public class PersonDetectController {

    private final PersonDetectService personDetectService;
    private static final Logger logger = LoggerFactory.getLogger(PersonDetectController.class);

    @Autowired
    public PersonDetectController(PersonDetectService personDetectService) {
        this.personDetectService = personDetectService;
    }

    // Example: GET /api/person-detect/status/roomA
    @GetMapping("/status/{roomType}")
    public ResponseEntity<?> getLatestPersonDetection(@PathVariable("roomType") String roomType) {
        logger.info("Getting latest the detection status for this room: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }

            logger.info("Latest Detection Activity: {}", personDetectService.getLatestPersonDetectionRecordByRoom(roomType));
            return ResponseEntity.ok(personDetectService.getLatestPersonDetectionRecordByRoom(roomType));
        }
        catch (RuntimeException e) {
            logger.error("No person detection data found for this room: {}.", roomType);
            return ResponseEntity.notFound().build(); // 404
        }
        catch (Exception e) {
            logger.error("Error occurred while getting latest person detection status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    // Example: GET /api/person-detect/status/all
    @GetMapping("/status/all")
    public ResponseEntity<?> getAllPersonDetectionRecords() {
        logger.info("Getting all person detection records...");

        try {
            return ResponseEntity.ok(personDetectService.getAllPersonDetectionRecords()); // Returns an empty list if no data found
        }
        catch (Exception e) {
            logger.error("Error occurred while getting all person detection records: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }

    // Example: POST /api/person-detect/create?persondetected=true&confidence=0.87&recordingtime=2024-11-28T00:35:22
    @PostMapping("/create")
    public ResponseEntity<?> createNewDetectionRecord(@Valid @RequestParam("roomtype") String roomType, @RequestParam("persondetected") Boolean personDetected, @RequestParam("confidence") Double confidence, @RequestParam("recordingtime") String recordingTime) {
        logger.info("Creating new person detection record for room: {}...", roomType);

        try {
            if (roomType == null || roomType.trim().isEmpty()) {
                logger.error("Room type is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Room type is empty or null.");
            }
            if (personDetected == null) {
                logger.error("Person detected is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Person detected is empty or null.");
            }
            if (confidence == null || confidence < 0) {
                logger.error("Confidence is invalid, aborting...");
                return ResponseEntity.badRequest().body("Confidence is invalid.");
            }
            if (recordingTime.isEmpty()) {
                logger.error("Recording time is empty or null, aborting...");
                return ResponseEntity.badRequest().body("Recording time is empty or null.");
            }

            PersonDetectionDTO personDetectionDTO = new PersonDetectionDTO();
            personDetectionDTO.setRoomType(roomType);
            personDetectionDTO.setPersonDetected(personDetected);
            personDetectionDTO.setConfidence(confidence);
            personDetectionDTO.setDetectionTime(recordingTime);

            personDetectService.recordPersonDetection(personDetectionDTO);

            return ResponseEntity.ok("Person detection record created successfully.");
        }
        catch (Exception e) {
            logger.error("Error occurred while creating new person detection record", e);
            return ResponseEntity.internalServerError().body("Error occurred while creating new person detection record."); // 500
        }
    }
}
