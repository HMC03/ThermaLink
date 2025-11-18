package com.iot.mobile_backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PersonDetectionDTO {
    @NotNull(message = "Person detected status is required")
    private Boolean personDetected;

    @NotNull(message = "Confidence is required")
    @DecimalMin(value = "0.0", message = "Confidence must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Confidence must be <= 1.0")
    private Double confidence;

    @NotBlank(message = "Detection time is required")
    private String detectionTime;
}
