package com.iot.mobile_backend.service;

import com.iot.mobile_backend.dto.FanDTO;
import com.iot.mobile_backend.model.FanSensor;
import com.iot.mobile_backend.repository.FanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FanService {

    private final FanRepository fanRepo;
    private final static Logger logger = LoggerFactory.getLogger(FanService.class);

    @Autowired
    public FanService(FanRepository fanRepo) {
        this.fanRepo = fanRepo;
    }

    public FanSensor getCurrentFanStatusByRoomType(String roomType) {
        logger.info("Checking current fan status for room type: {}...", roomType);

        return fanRepo.findFirstByOrderByRecordingTimeDesc()
                .orElseThrow(() -> new RuntimeException("No fan status data found for this room."));
    }

    public List<FanSensor> getAllFanStatusRecordsByRoomType() {
        logger.info("Fetching all fan status records...");

        List<FanSensor> fanStatusRecords = fanRepo.getAllRoomFanStatuses();

        if (fanStatusRecords.isEmpty()) {
            logger.warn("No fan status data found. Returning empty list...");
        }

        return fanStatusRecords;
    }

    public void recordFanStatus(FanDTO fanDTO) {
        logger.info("Recording new fan status for room: {}...", fanDTO.getRoomType());

        FanSensor newFanStatus = new FanSensor();
        newFanStatus.setRoomType(fanDTO.getRoomType());
        newFanStatus.setFanStatus(fanDTO.getFanStatus());
        newFanStatus.setRecordingTime(parseRecordingTime(fanDTO.getRecordingTime()));

        fanRepo.save(newFanStatus);
        logger.info("New fan status record for the room, {} has been added to database.", fanDTO.getRoomType());
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
