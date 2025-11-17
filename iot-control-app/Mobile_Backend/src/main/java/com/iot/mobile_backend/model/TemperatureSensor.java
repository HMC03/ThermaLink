package com.iot.mobile_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "temperature_activity")
public class TemperatureSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(name = "temperature_f",nullable = false)
    private Double temperature;

    @Column(nullable = false, name = "recording_time")
    private LocalDateTime recordingTime;
}
