package com.iot.mobile_backend.service;

import com.iot.mobile_backend.model.PersonDetection;
import com.iot.mobile_backend.repository.PersonDetectRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonDetectService {

    private final PersonDetectRepo personDetectRepo;
    private final static Logger logger = LoggerFactory.getLogger(PersonDetectService.class);

    @Autowired
    public PersonDetectService(PersonDetectRepo personDetectRepo) {
        this.personDetectRepo = personDetectRepo;
    }

    public Boolean getLatestPersonDetection() {
        logger.info("Checking current person detection status...");

        Optional<PersonDetection> latestActivity = personDetectRepo.findFirstByOrderByDetectionTimeDesc();

        if (latestActivity.isPresent()) {
            PersonDetection detection = latestActivity.get();

            if (detection.getPersonDetected() && detection.getConfidence() >= 0.65) {
                logger.info("Person detected!");
                return true;
            }
            else if (detection.getConfidence() < 0.65) {
                logger.info("Confidence too low, default to false.");
                return false;
            }
            else {
                logger.info("Person not detection!");
                return false;
            }
        }

        logger.warn("No person detection data found. Returning default value: false...");
        return false;
    }
}
