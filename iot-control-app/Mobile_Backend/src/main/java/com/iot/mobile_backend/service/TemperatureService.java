package com.iot.mobile_backend.service;

import com.iot.mobile_backend.dto.TemperatureDTO;
import com.iot.mobile_backend.model.TemperatureSensor;
import com.iot.mobile_backend.repository.TempRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemperatureService {

    private final TempRepository tempRepository;
    private final static Logger logger = LoggerFactory.getLogger(TemperatureService.class);

    @Autowired
    public TemperatureService(TempRepository tempRepository) {
        this.tempRepository = tempRepository;
    }

    public TemperatureSensor getCurrentTemperatureByRoomType(String roomType) {
        logger.info("Getting current temperature for room type: {}...", roomType);

        return tempRepository.findFirstByRoomTypeOrderByRecordingTimeDesc(roomType)
                .orElseThrow(() -> new RuntimeException("No temperature data found for room: " + roomType ));
    }

    public List<TemperatureSensor> getAllRoomTemperatures() {
        logger.info("Fetching all room temperatures from the database...");

        List<TemperatureSensor> roomTempListByRoomType = tempRepository.getAllRoomTemperatures();

        if (roomTempListByRoomType.isEmpty()) {
            logger.warn("No temperature data found. Returning empty list...");
        }

        return roomTempListByRoomType;
    }

    public void recordTemperature(TemperatureDTO temperatureDTO) {
        logger.info("Recording new temperature for room: {}...", temperatureDTO.getRoomType());

        TemperatureSensor newTemp = new TemperatureSensor();
        newTemp.setRoomType(temperatureDTO.getRoomType());
        newTemp.setTemperature(temperatureDTO.getTemperature());
        newTemp.setRecordingTime(parseRecordingTime(temperatureDTO.getRecordingTime()));

        tempRepository.save(newTemp);
        logger.info("Temperature record has been added to database.");
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
