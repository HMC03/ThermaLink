package com.iot.mobile_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HeaterDTO {
    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Heater status is required")
    private Boolean heaterStatus;

    @NotBlank(message = "Recording time is required")
    private String recordingTime;
}
