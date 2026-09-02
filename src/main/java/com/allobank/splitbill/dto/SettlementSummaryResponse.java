package com.allobank.splitbill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record SettlementSummaryResponse(
        @JsonProperty("group_id") Long groupId,
        @JsonProperty("group_name") String groupName,
        @JsonProperty("total_expenses") BigDecimal totalExpenses,
        @JsonProperty("service_charge_pct") int serviceChargePct,
        @JsonProperty("service_charge_amount") BigDecimal serviceChargeAmount,
        List<SettlementTransactionResponse> settlements
) {}
