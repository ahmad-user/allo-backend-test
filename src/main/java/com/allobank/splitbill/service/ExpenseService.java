package com.allobank.splitbill.service;

import com.allobank.splitbill.domain.ExpenseEntity;
import com.allobank.splitbill.domain.GroupEntity;
import com.allobank.splitbill.domain.ParticipantEntity;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.exception.BadRequestException;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final GroupService groupService;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(GroupService groupService, ExpenseRepository expenseRepository) {
        this.groupService = groupService;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse addExpense(Long groupId, AddExpenseRequest request) {
        GroupEntity group = groupService.getGroupEntity(groupId);

        if (request.description() == null || request.description().isBlank()) {
            throw new BadRequestException("Description is required and cannot be blank");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        if (request.participantIds() == null || request.participantIds().isEmpty()) {
            throw new BadRequestException("participantIds must not be empty");
        }

        // Check duplicate participant IDs
        Set<Long> uniqueParticipantIds = new HashSet<>();
        for (Long id : request.participantIds()) {
            if (!uniqueParticipantIds.add(id)) {
                throw new BadRequestException("Duplicate participant ID found in request: " + id);
            }
        }

        Map<Long, ParticipantEntity> groupParticipantsMap = group.getParticipants().stream()
                .collect(Collectors.toMap(ParticipantEntity::getId, p -> p));

        ParticipantEntity paidBy = groupParticipantsMap.get(request.paidByParticipantId());
        if (paidBy == null) {
            throw new BadRequestException("Participant with ID " + request.paidByParticipantId() + " does not belong to group " + groupId);
        }

        Set<ParticipantEntity> sharingParticipants = new HashSet<>();
        for (Long id : request.participantIds()) {
            ParticipantEntity participant = groupParticipantsMap.get(id);
            if (participant == null) {
                throw new BadRequestException("Participant with ID " + id + " does not belong to group " + groupId);
            }
            sharingParticipants.add(participant);
        }

        ExpenseEntity expense = new ExpenseEntity();
        expense.setGroup(group);
        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setPaidBy(paidBy);
        expense.setParticipants(sharingParticipants);

        group.addExpense(expense);
        ExpenseEntity savedExpense = expenseRepository.save(expense);

        List<Long> sharingParticipantIds = savedExpense.getParticipants().stream()
                .map(ParticipantEntity::getId)
                .sorted()
                .toList();

        return new ExpenseResponse(
                savedExpense.getId(),
                group.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount(),
                savedExpense.getPaidBy().getId(),
                sharingParticipantIds,
                savedExpense.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroupId(Long groupId) {
        groupService.getGroupEntity(groupId);
        List<ExpenseEntity> expenses = expenseRepository.findByGroupIdWithDetails(groupId);
        return expenses.stream()
                .map(expense -> {
                    List<Long> sharingParticipantIds = expense.getParticipants().stream()
                            .map(ParticipantEntity::getId)
                            .sorted()
                            .toList();
                    return new ExpenseResponse(
                            expense.getId(),
                            expense.getGroup().getId(),
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getPaidBy().getId(),
                            sharingParticipantIds,
                            expense.getCreatedAt()
                    );
                })
                .toList();
    }
}
