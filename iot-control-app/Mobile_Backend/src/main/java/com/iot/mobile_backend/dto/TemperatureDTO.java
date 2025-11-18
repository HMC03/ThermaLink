package com.iot.mobile_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemperatureDTO {
    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Temperature is required")
    private Double temperature;

    @NotBlank(message = "Recording time is required")
    private String recordingTime;
}
