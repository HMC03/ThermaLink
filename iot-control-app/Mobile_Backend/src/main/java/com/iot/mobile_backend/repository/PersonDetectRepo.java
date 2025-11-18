package com.iot.mobile_backend.repository;

import com.iot.mobile_backend.model.PersonDetection;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface PersonDetectRepo extends JpaRepository<PersonDetection, Long> {

    Optional<PersonDetection> findFirstByOrderByDetectionTimeDesc();
}
