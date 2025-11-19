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

    @GetMapping("/status")
    public ResponseEntity<?> getLatestPersonDetection() {
        logger.info("Getting latest the detection status...");

        try {
            logger.info("Latest Detection Activity: {}", personDetectService.getLatestPersonDetectionRecord());
            return ResponseEntity.ok(personDetectService.getLatestPersonDetectionRecord());
        }
        catch (RuntimeException e) {
            logger.error("No person detection data found.");
            return ResponseEntity.notFound().build(); // 404
        }
        catch (Exception e) {
            logger.error("Error occurred while getting latest person detection status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Internal server error."); // 500
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNewDetectionRecord(@Valid @RequestParam("persondetected") Boolean personDetected, @RequestParam("confidence") Double confidence, @RequestParam("recordingtime") String recordingTime) {
        logger.info("Creating new person detection record...");

        try {
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
