package com.allobank.splitbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateGroupRequest(
        @NotBlank(message = "Group name is required")
        String name,

        @NotEmpty(message = "Participant list is required and cannot be empty")
        @Valid
        List<ParticipantRequest> participants
) {}
