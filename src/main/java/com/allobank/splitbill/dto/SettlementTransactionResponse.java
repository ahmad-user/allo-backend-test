package com.allobank.splitbill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record SettlementTransactionResponse(
        @JsonProperty("from_participant_id") Long fromParticipantId,
        @JsonProperty("from_participant_name") String fromParticipantName,
        @JsonProperty("to_participant_id") Long toParticipantId,
        @JsonProperty("to_participant_name") String toParticipantName,
        BigDecimal amount
) {}
