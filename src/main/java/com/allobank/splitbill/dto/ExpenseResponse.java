package com.allobank.splitbill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExpenseResponse(
        Long id,
        @JsonProperty("group_id") Long groupId,
        String description,
        BigDecimal amount,
        @JsonProperty("paid_by_participant_id") Long paidByParticipantId,
        @JsonProperty("participant_ids") List<Long> participantIds,
        @JsonProperty("created_at") Instant createdAt
) {}
