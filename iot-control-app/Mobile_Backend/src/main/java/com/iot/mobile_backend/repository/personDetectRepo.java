package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.PersonDetection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface personDetectRepo extends JpaRepository<PersonDetection, Long> {

    /* NOTE: Service class will extract the recent person activity from the list. */
    @Query("SELECT p FROM PersonDetection p WHERE p.personDetected = :personDetected AND p.confidence >= :confidence ORDER BY p.detectionTime DESC")
    List<PersonDetection> getCurrentPersonDetected(boolean personDetected, Double confidence, Pageable pageable);
}
