package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.PersonDetection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonDetectRepo extends JpaRepository<PersonDetection, Long> {

    // Fetch the latest detection on the database, sorted by detection time from each room type.
    Optional<PersonDetection> findFirstByRoomTypeOrderByDetectionTimeDesc(String roomType);

    // Fetch all detections for all rooms, sorted by room type and detection time.
    @Query("SELECT p FROM PersonDetection p WHERE p.id IN (SELECT MAX(p2.id) FROM PersonDetection p2 GROUP BY p2.roomType)")
    List<PersonDetection> getAllRoomDetections();
}
