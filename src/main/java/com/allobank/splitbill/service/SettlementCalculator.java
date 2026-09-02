package com.allobank.splitbill.service;

import com.allobank.splitbill.domain.ExpenseEntity;
import com.allobank.splitbill.domain.ParticipantEntity;
import com.allobank.splitbill.dto.SettlementTransactionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SettlementCalculator {

    public static class ParticipantBalance {
        private final ParticipantEntity participant;
        private BigDecimal netBalance;

        public ParticipantBalance(ParticipantEntity participant, BigDecimal netBalance) {
            this.participant = participant;
            this.netBalance = netBalance;
        }

        public ParticipantEntity getParticipant() {
            return participant;
        }

        public BigDecimal getNetBalance() {
            return netBalance;
        }

        public void setNetBalance(BigDecimal netBalance) {
            this.netBalance = netBalance;
        }
    }

    public List<SettlementTransactionResponse> calculateSettlements(
            List<ParticipantEntity> allParticipants,
            List<ExpenseEntity> expenses
    ) {
        Map<Long, ParticipantEntity> participantMap = new HashMap<>();
        Map<Long, BigDecimal> netBalances = new HashMap<>();

        for (ParticipantEntity p : allParticipants) {
            participantMap.put(p.getId(), p);
            netBalances.put(p.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        for (ExpenseEntity expense : expenses) {
            BigDecimal amount = expense.getAmount().setScale(2, RoundingMode.HALF_UP);
            ParticipantEntity payer = expense.getPaidBy();

            // Payer gets credit for full amount paid
            netBalances.put(payer.getId(), netBalances.get(payer.getId()).add(amount));

            Set<ParticipantEntity> sharers = expense.getParticipants();
            if (sharers == null || sharers.isEmpty()) {
                continue;
            }

            List<ParticipantEntity> sortedSharers = sharers.stream()
                    .sorted(Comparator.comparing(ParticipantEntity::getId))
                    .toList();

            int n = sortedSharers.size();
            BigDecimal count = BigDecimal.valueOf(n);

            BigDecimal baseShare = amount.divide(count, 2, RoundingMode.DOWN);
            BigDecimal totalAssigned = baseShare.multiply(count);
            BigDecimal remainder = amount.subtract(totalAssigned);
            int remainderCents = remainder.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();

            for (int i = 0; i < n; i++) {
                ParticipantEntity sharer = sortedSharers.get(i);
                BigDecimal share = baseShare;
                if (i < remainderCents) {
                    share = share.add(new BigDecimal("0.01"));
                }
                netBalances.put(sharer.getId(), netBalances.get(sharer.getId()).subtract(share));
            }
        }

        List<ParticipantBalance> debtors = new ArrayList<>();
        List<ParticipantBalance> creditors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            ParticipantEntity p = participantMap.get(entry.getKey());
            BigDecimal balance = entry.getValue().setScale(2, RoundingMode.HALF_UP);

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                // Debt is absolute value of net balance
                debtors.add(new ParticipantBalance(p, balance.abs()));
            } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new ParticipantBalance(p, balance));
            }
        }

        List<SettlementTransactionResponse> settlements = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            debtors.sort(Comparator.comparing(ParticipantBalance::getNetBalance).reversed()
                    .thenComparing(pb -> pb.getParticipant().getId()));
            creditors.sort(Comparator.comparing(ParticipantBalance::getNetBalance).reversed()
                    .thenComparing(pb -> pb.getParticipant().getId()));

            ParticipantBalance maxDebtor = debtors.get(0);
            ParticipantBalance maxCreditor = creditors.get(0);

            BigDecimal settleAmount = maxDebtor.getNetBalance().min(maxCreditor.getNetBalance())
                    .setScale(2, RoundingMode.HALF_UP);

            if (settleAmount.compareTo(BigDecimal.ZERO) > 0) {
                settlements.add(new SettlementTransactionResponse(
                        maxDebtor.getParticipant().getId(),
                        maxDebtor.getParticipant().getName(),
                        maxCreditor.getParticipant().getId(),
                        maxCreditor.getParticipant().getName(),
                        settleAmount
                ));
            }

            maxDebtor.setNetBalance(maxDebtor.getNetBalance().subtract(settleAmount));
            maxCreditor.setNetBalance(maxCreditor.getNetBalance().subtract(settleAmount));

            if (maxDebtor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) {
                debtors.remove(0);
            }
            if (maxCreditor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) {
                creditors.remove(0);
            }
        }

        return settlements;
    }
}
