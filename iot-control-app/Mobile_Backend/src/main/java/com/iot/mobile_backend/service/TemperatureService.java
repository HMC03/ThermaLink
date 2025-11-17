package com.iot.mobile_backend.service;

import com.iot.mobile_backend.model.TemperatureSensor;
import com.iot.mobile_backend.repository.TempRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        logger.info("Getting all room temperatures...");

        List<TemperatureSensor> roomTempListByRoomType = tempRepository.getAllRoomTemperatures();

        if (roomTempListByRoomType.isEmpty()) {
            logger.warn("No temperature data found. Returning empty list...");
        }

        return roomTempListByRoomType;
    }
}
