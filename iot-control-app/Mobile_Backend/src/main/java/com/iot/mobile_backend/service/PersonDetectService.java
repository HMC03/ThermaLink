package com.iot.mobile_backend.service;

import com.iot.mobile_backend.dto.PersonDetectionDTO;
import com.iot.mobile_backend.model.PersonDetection;
import com.iot.mobile_backend.repository.PersonDetectRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PersonDetectService {

    private final PersonDetectRepo personDetectRepo;
    private final static Logger logger = LoggerFactory.getLogger(PersonDetectService.class);

    @Autowired
    public PersonDetectService(PersonDetectRepo personDetectRepo) {
        this.personDetectRepo = personDetectRepo;
    }

    public PersonDetection getLatestPersonDetectionRecord()
    {
        logger.info("Checking current person detection status...");

        return personDetectRepo.findFirstByOrderByDetectionTimeDesc()
                .orElseThrow(() -> new RuntimeException("No person detection data found."));
    }

    public void recordPersonDetection(PersonDetectionDTO detectionDTO) {
        logger.info("Recording new person detection activity at time: {}...", detectionDTO.getDetectionTime());

        PersonDetection newDetection = new PersonDetection();
        newDetection.setPersonDetected(detectionDTO.getPersonDetected());
        newDetection.setConfidence(detectionDTO.getConfidence());
        newDetection.setDetectionTime(parseRecordingTime(detectionDTO.getDetectionTime()));

        personDetectRepo.save(newDetection);
        logger.info("New detected activity has been added to database.");
    }

    private LocalDateTime parseRecordingTime(String recordingTime) {
        if (recordingTime.isEmpty()) {
            logger.warn("Recording time is empty or null, defaulting to current time.");
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(recordingTime);  // ISO format: yyyy-MM-ddTHH:mm:ss
        }
        catch (Exception e) {
            logger.warn("Invalid recording time format, defaulting to current time.");
            return LocalDateTime.now();
        }
    }
}
