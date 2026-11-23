package com.qrtesy.modules.checkin.features.scan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanTicketRequest(
        @NotBlank(message = "Ticket code is required.")
        @Size(max = 32, message = "Ticket code must not exceed 32 characters.")
        String ticketCode
) {
}