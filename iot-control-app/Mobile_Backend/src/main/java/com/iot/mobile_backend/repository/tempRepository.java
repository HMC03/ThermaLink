package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.TemperatureSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface tempRepository extends JpaRepository<TemperatureSensor, Long> {

    @Query("SELECT t FROM TemperatureSensor t WHERE t.roomType = :room_type ORDER BY t.recordingTime DESC LIMIT 1")
    Optional<TemperatureSensor> getCurrTempByRoomType(@Param("room_type") String roomType);

    @Query("SELECT t FROM TemperatureSensor t WHERE t.id IN (SELECT MAX(t2.id) FROM TemperatureSensor t2 GROUP BY t2.roomType)")
    List<TemperatureSensor> getAllRoomTemperatures();
}
