package com.iot.mobile_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "person_detection")
public class PersonDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="person_detected")
    private Boolean personDetected;

    @Column
    private Double confidence;

    @Column(nullable = false, name="detection_time")
    private LocalDateTime detectionTime;
}
