package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.HeaterSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeaterRepository extends JpaRepository<HeaterSensor, Long> {

    // Fetch the latest heater status on the database, sorted by recording time.
    Optional<HeaterSensor> findFirstByOrderByRecordingTimeDesc();

    // Fetch all heater statuses for all rooms, sorted by room type and recording time.
    @Query("SELECT h FROM HeaterSensor h WHERE h.id IN (SELECT MAX(h2.id) FROM HeaterSensor h2 GROUP BY h2.roomType)")
    List<HeaterSensor> getAllRoomHeaterStatuses();
}
