package com.iot.mobile_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordingTime;
}
