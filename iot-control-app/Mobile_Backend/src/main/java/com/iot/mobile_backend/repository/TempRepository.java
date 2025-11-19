package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.TemperatureSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempRepository extends JpaRepository<TemperatureSensor, Long> {

    // Fetch the latest temperature reading on the database, sorted by recording time.
    Optional<TemperatureSensor> findFirstByRoomTypeOrderByRecordingTimeDesc(String roomType);

    // Fetch all temperature readings for all rooms, sorted by room type and recording time.
    @Query("SELECT t FROM TemperatureSensor t WHERE t.id IN (SELECT MAX(t2.id) FROM TemperatureSensor t2 GROUP BY t2.roomType)")
    List<TemperatureSensor> getAllRoomTemperatures();
}
