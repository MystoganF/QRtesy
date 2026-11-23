package com.qrtesy.modules.events.features.manage;

import com.qrtesy.modules.events.domain.EventStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record SaveEventRequest(
        @NotBlank(message = "Event title is required.")
        @Size(max = 160, message = "Title must not exceed 160 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        String description,

        @NotBlank(message = "Venue is required.")
        @Size(max = 200, message = "Venue must not exceed 200 characters.")
        String venue,

        @NotNull(message = "Event date is required.")
        LocalDate eventDate,

        @NotNull(message = "Start time is required.")
        LocalTime startTime,

        @NotNull(message = "End time is required.")
        LocalTime endTime,

        @NotNull(message = "Capacity is required.")
        @Min(value = 1, message = "Capacity must be at least 1.")
        Integer capacity,

        @NotNull(message = "Status is required.")
        EventStatus status
) {
}