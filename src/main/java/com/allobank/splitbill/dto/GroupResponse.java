package com.allobank.splitbill.dto;

import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        List<ParticipantResponse> participants
) {}
