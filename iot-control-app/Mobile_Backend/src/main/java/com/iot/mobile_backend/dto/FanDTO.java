package com.iot.mobile_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FanDTO {
    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Fan status is required")
    private Boolean fanStatus;

    @NotBlank(message = "Recording time is required")
    private String recordingTime;
}
