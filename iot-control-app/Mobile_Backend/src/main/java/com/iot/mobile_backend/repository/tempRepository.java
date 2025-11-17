package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.TemperatureSensor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface tempRepository extends JpaRepository<TemperatureSensor, Long> {

    /* NOTE: Service class will extract the current temperature from the list. */
    @Query("SELECT t FROM TemperatureSensor t WHERE t.roomType = :roomType ORDER BY t.recordingTime DESC")
    List<TemperatureSensor> getCurrTempByRoomType(String roomType, Pageable pageable);

    @Query("SELECT t FROM TemperatureSensor t WHERE t.id IN (SELECT MAX(t2.id) FROM TemperatureSensor t2 GROUP BY t2.roomType)")
    List<TemperatureSensor> getAllRoomTemperatures();
}
