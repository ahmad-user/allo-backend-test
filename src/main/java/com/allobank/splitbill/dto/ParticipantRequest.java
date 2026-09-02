package com.allobank.splitbill.dto;

import jakarta.validation.constraints.NotBlank;

public record ParticipantRequest(
        @NotBlank(message = "Participant name is required")
        String name
) {}
