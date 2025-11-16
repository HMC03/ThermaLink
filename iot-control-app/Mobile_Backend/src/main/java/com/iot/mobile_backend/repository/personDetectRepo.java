package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.PersonDetection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface personDetectRepo extends JpaRepository<PersonDetection, Long> {

    @Query("SELECT p FROM PersonDetection p WHERE p.personDetected = :person_detected AND p.confidence >= :confidence ORDER BY p.detectionTime DESC LIMIT 1")
    Optional<PersonDetection> getCurrentPersonDetected(boolean personDetected, Double confidence);
}
