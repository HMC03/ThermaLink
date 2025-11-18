package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.TemperatureSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempRepository extends JpaRepository<TemperatureSensor, Long> {

    Optional<TemperatureSensor> findFirstByRoomTypeOrderByRecordingTimeDesc(String roomType);

    @Query("SELECT t FROM TemperatureSensor t WHERE t.id IN (SELECT MAX(t2.id) FROM TemperatureSensor t2 GROUP BY t2.roomType)")
    List<TemperatureSensor> getAllRoomTemperatures();
}
