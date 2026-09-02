package com.allobank.splitbill.service;

import com.allobank.splitbill.domain.ExpenseEntity;
import com.allobank.splitbill.domain.GroupEntity;
import com.allobank.splitbill.domain.ParticipantEntity;
import com.allobank.splitbill.dto.SettlementSummaryResponse;
import com.allobank.splitbill.dto.SettlementTransactionResponse;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SettlementService {

    private final GroupService groupService;
    private final ExpenseRepository expenseRepository;
    private final SettlementCalculator settlementCalculator;
    private final ServiceChargeCalculator serviceChargeCalculator;

    public SettlementService(
            GroupService groupService,
            ExpenseRepository expenseRepository,
            SettlementCalculator settlementCalculator,
            ServiceChargeCalculator serviceChargeCalculator
    ) {
        this.groupService = groupService;
        this.expenseRepository = expenseRepository;
        this.settlementCalculator = settlementCalculator;
        this.serviceChargeCalculator = serviceChargeCalculator;
    }

    @Transactional(readOnly = true)
    public SettlementSummaryResponse getSettlementSummary(Long groupId) {
        GroupEntity group = groupService.getGroupEntity(groupId);
        List<ParticipantEntity> participants = group.getParticipants();
        List<ExpenseEntity> expenses = expenseRepository.findByGroupIdWithDetails(groupId);

        BigDecimal totalExpenses = expenses.stream()
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        int serviceChargePct = serviceChargeCalculator.getServiceChargePct();
        BigDecimal serviceChargeAmount = serviceChargeCalculator.calculateServiceChargeAmount(totalExpenses, serviceChargePct);

        List<SettlementTransactionResponse> settlements = settlementCalculator.calculateSettlements(participants, expenses);

        return new SettlementSummaryResponse(
                group.getId(),
                group.getName(),
                totalExpenses,
                serviceChargePct,
                serviceChargeAmount,
                settlements
        );
    }
}
