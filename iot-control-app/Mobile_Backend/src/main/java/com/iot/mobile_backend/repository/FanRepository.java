package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.FanSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FanRepository extends JpaRepository<FanSensor, Long> {

    // Fetch the latest fan status on the database, sorted by recording time.
    Optional<FanSensor> findFirstByOrderByRecordingTimeDesc();

    // Fetch all fan statuses for all rooms, sorted by room type and recording time.
    @Query("SELECT f FROM FanSensor f WHERE f.id IN (SELECT MAX(f2.id) FROM FanSensor f2 GROUP BY f2.roomType)")
    List<FanSensor> getAllRoomFanStatuses();
}
