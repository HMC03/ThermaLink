package com.iot.mobile_backend.service;

import com.iot.mobile_backend.dto.PersonDetectionDTO;
import com.iot.mobile_backend.model.PersonDetection;
import com.iot.mobile_backend.repository.PersonDetectRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PersonDetectService {

    private final PersonDetectRepo personDetectRepo;
    private final static Logger logger = LoggerFactory.getLogger(PersonDetectService.class);

    @Autowired
    public PersonDetectService(PersonDetectRepo personDetectRepo) {
        this.personDetectRepo = personDetectRepo;
    }

    public PersonDetection getLatestPersonDetectionRecordByRoom(String roomType)
    {
        logger.info("Checking current person detection status for this room: {}...", roomType);

        return personDetectRepo.findFirstByRoomTypeOrderByDetectionTimeDesc(roomType)
                .orElseThrow(() -> new RuntimeException("No person detection data found for this room."));
    }

    public List<PersonDetection> getAllPersonDetectionRecords() {
        logger.info("Fetching all person detection records in each room...");

        List<PersonDetection> personDetections = personDetectRepo.getAllRoomDetections();

        if (personDetections.isEmpty()) {
            logger.warn("No person detection data found.");
        }

        return personDetections;
    }

    public void recordPersonDetection(PersonDetectionDTO detectionDTO) {
        logger.info("Recording new person detection activity at time: {} for this room, {}...", detectionDTO.getDetectionTime(), detectionDTO.getRoomType());

        PersonDetection newDetection = new PersonDetection();
        newDetection.setRoomType(detectionDTO.getRoomType());
        newDetection.setPersonDetected(detectionDTO.getPersonDetected());
        newDetection.setConfidence(detectionDTO.getConfidence());
        newDetection.setDetectionTime(parseRecordingTime(detectionDTO.getDetectionTime()));

        personDetectRepo.save(newDetection);
        logger.info("New detected activity at the room, {} has been added to database.", detectionDTO.getRoomType());
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
