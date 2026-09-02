package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.dto.SettlementSummaryResponse;
import com.allobank.splitbill.service.ExpenseService;
import com.allobank.splitbill.service.GroupService;
import com.allobank.splitbill.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;

    public GroupController(
            GroupService groupService,
            ExpenseService expenseService,
            SettlementService settlementService
    ) {
        this.groupService = groupService;
        this.expenseService = expenseService;
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId) {
        GroupResponse response = groupService.getGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody AddExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.addExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable Long groupId) {
        List<ExpenseResponse> response = expenseService.getExpensesByGroupId(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/settlement")
    public ResponseEntity<SettlementSummaryResponse> getSettlementSummary(@PathVariable Long groupId) {
        SettlementSummaryResponse response = settlementService.getSettlementSummary(groupId);
        return ResponseEntity.ok(response);
    }
}
