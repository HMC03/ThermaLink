package com.iot.mobile_backend.service;

import com.iot.mobile_backend.dto.HeaterDTO;
import com.iot.mobile_backend.model.HeaterSensor;
import com.iot.mobile_backend.repository.HeaterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HeaterService {

    private final HeaterRepository heaterRepo;
    private final static Logger logger = LoggerFactory.getLogger(HeaterService.class);

    @Autowired
    public HeaterService(HeaterRepository heaterRepo) {
        this.heaterRepo = heaterRepo;
    }

    public HeaterSensor getCurrentHeaterStatusByRoomType(String roomType) {
        logger.info("Checking current heater status for room type: {}...", roomType);

        return heaterRepo.findFirstByOrderByRecordingTimeDesc()
                .orElseThrow(() -> new RuntimeException("No heater status data found for this room."));
    }

    public List<HeaterSensor> getAllHeaterStatusRecordsByRoomType() {
        logger.info("Fetching all heater status records...");

        List<HeaterSensor> heaterStatusRecords = heaterRepo.getAllRoomHeaterStatuses();

        if (heaterStatusRecords.isEmpty()) {
            logger.warn("No heater status data found. Returning empty list...");
        }

        return heaterStatusRecords;
    }

    public void recordHeaterStatus(HeaterDTO heaterDTO) {
        logger.info("Recording new heater status for room: {}...", heaterDTO.getRoomType());

        HeaterSensor newHeaterStatus = new HeaterSensor();
        newHeaterStatus.setRoomType(heaterDTO.getRoomType());
        newHeaterStatus.setHeaterStatus(heaterDTO.getHeaterStatus());
        newHeaterStatus.setRecordingTime(parseRecordingTime(heaterDTO.getRecordingTime()));

        heaterRepo.save(newHeaterStatus);
        logger.info("New heater status record for the room, {} has been added to database.", heaterDTO.getRoomType());
    }

    private LocalDateTime parseRecordingTime(String recordingTime) {
        if (recordingTime == null || recordingTime.trim().isEmpty()) {
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
