package com.allobank.splitbill.service;

import com.allobank.splitbill.domain.ExpenseEntity;
import com.allobank.splitbill.domain.GroupEntity;
import com.allobank.splitbill.domain.ParticipantEntity;
import com.allobank.splitbill.dto.SettlementTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementCalculatorTest {

    private SettlementCalculator calculator;
    private GroupEntity group;
    private ParticipantEntity alice;
    private ParticipantEntity bob;
    private ParticipantEntity charlie;
    private ParticipantEntity david;

    @BeforeEach
    void setUp() {
        calculator = new SettlementCalculator();

        group = new GroupEntity("Test Group");
        group.setId(1L);

        alice = new ParticipantEntity("Alice", group);
        alice.setId(1L);
        bob = new ParticipantEntity("Bob", group);
        bob.setId(2L);
        charlie = new ParticipantEntity("Charlie", group);
        charlie.setId(3L);
        david = new ParticipantEntity("David", group);
        david.setId(4L);

        group.addParticipant(alice);
        group.addParticipant(bob);
        group.addParticipant(charlie);
        group.addParticipant(david);
    }

    @Test
    @DisplayName("Scenario 1: One payer, equal split among 3 participants")
    void testOnePayerEqualSplit() {
        // Alice pays 300000.00 shared by Alice, Bob, Charlie
        ExpenseEntity expense = createExpense("Dinner", "300000.00", alice, Set.of(alice, bob, charlie));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie),
                List.of(expense)
        );

        assertEquals(2, settlements.size());

        // Bob -> Alice 100000.00
        // Charlie -> Alice 100000.00
        BigDecimal bobToAlice = findSettlementAmount(settlements, 2L, 1L);
        BigDecimal charlieToAlice = findSettlementAmount(settlements, 3L, 1L);

        assertEquals(new BigDecimal("100000.00"), bobToAlice);
        assertEquals(new BigDecimal("100000.00"), charlieToAlice);
    }

    @Test
    @DisplayName("Scenario 2: Multiple payers sequential expenses")
    void testMultiplePayers() {
        // Expense 1: Alice pays 300 for Alice, Bob, Charlie -> Bob owes 100, Charlie owes 100, Alice +200
        ExpenseEntity expense1 = createExpense("Dinner", "300.00", alice, Set.of(alice, bob, charlie));
        // Expense 2: Bob pays 90 for Bob, Charlie -> Bob paid 90, Bob share 45 -> Bob net +45. Charlie share 45 -> Charlie owes 45.
        // Combined net balances:
        // Alice: paid 300, share 100 -> net +200
        // Bob: paid 90, share 145 -> net -55
        // Charlie: paid 0, share 145 -> net -145
        ExpenseEntity expense2 = createExpense("Taxi", "90.00", bob, Set.of(bob, charlie));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie),
                List.of(expense1, expense2)
        );

        assertEquals(2, settlements.size());
        assertEquals(new BigDecimal("55.00"), findSettlementAmount(settlements, 2L, 1L)); // Bob -> Alice 55
        assertEquals(new BigDecimal("145.00"), findSettlementAmount(settlements, 3L, 1L)); // Charlie -> Alice 145
    }

    @Test
    @DisplayName("Scenario 3: Different participant subsets per expense")
    void testDifferentParticipantSubsets() {
        // Alice pays 100 for Alice & Bob (50 each)
        ExpenseEntity e1 = createExpense("Lunch", "100.00", alice, Set.of(alice, bob));
        // Charlie pays 120 for Charlie & David (60 each)
        ExpenseEntity e2 = createExpense("Movie", "120.00", charlie, Set.of(charlie, david));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie, david),
                List.of(e1, e2)
        );

        assertEquals(2, settlements.size());
        assertEquals(new BigDecimal("50.00"), findSettlementAmount(settlements, 2L, 1L)); // Bob -> Alice 50
        assertEquals(new BigDecimal("60.00"), findSettlementAmount(settlements, 4L, 3L)); // David -> Charlie 60
    }

    @Test
    @DisplayName("Scenario 4: Already balanced group returns empty settlements")
    void testAlreadyBalancedGroup() {
        // Alice pays 100 for Bob, Bob pays 100 for Alice
        ExpenseEntity e1 = createExpense("Gift 1", "100.00", alice, Set.of(bob));
        ExpenseEntity e2 = createExpense("Gift 2", "100.00", bob, Set.of(alice));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob),
                List.of(e1, e2)
        );

        assertTrue(settlements.isEmpty());
    }

    @Test
    @DisplayName("Scenario 5: Decimal amounts precision verification")
    void testDecimalAmounts() {
        ExpenseEntity e = createExpense("Groceries", "123.45", alice, Set.of(alice, bob, charlie));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie),
                List.of(e)
        );

        BigDecimal totalSettled = settlements.stream()
                .map(SettlementTransactionResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Alice share: 41.15, Bob share: 41.15, Charlie share: 41.15 (123.45 / 3 = 41.15 exactly)
        assertEquals(new BigDecimal("82.30"), totalSettled); // Bob (41.15) + Charlie (41.15) -> Alice
    }

    @Test
    @DisplayName("Scenario 6: Rounding scenario 100 / 3 gives deterministic exact sum")
    void testRoundingScenario100DividedBy3() {
        // 100.00 / 3 -> base share 33.33, remainder 0.01 assigned to Alice (first participant)
        // Alice share: 33.34, Bob share: 33.33, Charlie share: 33.33
        // Alice paid 100.00. Net Alice = +66.66. Net Bob = -33.33. Net Charlie = -33.33.
        ExpenseEntity e = createExpense("Shared item", "100.00", alice, Set.of(alice, bob, charlie));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie),
                List.of(e)
        );

        assertEquals(2, settlements.size());

        BigDecimal bobToAlice = findSettlementAmount(settlements, 2L, 1L);
        BigDecimal charlieToAlice = findSettlementAmount(settlements, 3L, 1L);

        assertEquals(new BigDecimal("33.33"), bobToAlice);
        assertEquals(new BigDecimal("33.33"), charlieToAlice);

        BigDecimal sumSettlements = bobToAlice.add(charlieToAlice);
        assertEquals(new BigDecimal("66.66"), sumSettlements);
    }

    @Test
    @DisplayName("Scenario 7 & 9: Multiple creditors/debtors transaction minimization")
    void testMultipleCreditorsAndDebtorsMinimization() {
        // Alice pays 400 for Alice, Bob, Charlie, David (100 each) -> Alice net +300
        ExpenseEntity e1 = createExpense("Resort", "400.00", alice, Set.of(alice, bob, charlie, david));
        // Bob pays 200 for Alice, Bob, Charlie, David (50 each) -> Bob net +150
        ExpenseEntity e2 = createExpense("Activity", "200.00", bob, Set.of(alice, bob, charlie, david));

        // Balances:
        // Alice: paid 400, share 150 -> net +250
        // Bob: paid 200, share 150 -> net +50
        // Charlie: paid 0, share 150 -> net -150
        // David: paid 0, share 150 -> net -150

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie, david),
                List.of(e1, e2)
        );

        // Minimized transactions should be 3 or fewer (4 participants max transactions = 3)
        assertTrue(settlements.size() <= 3);

        BigDecimal totalDebts = settlements.stream()
                .map(SettlementTransactionResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("300.00"), totalDebts); // Charlie 150 + David 150 = 300.00
    }

    @Test
    @DisplayName("Scenario 8: Zero balance participant handling")
    void testZeroBalanceParticipant() {
        // Alice pays 100 for Alice & Bob (50 each). Charlie is in group but has 0 expenses.
        ExpenseEntity e = createExpense("Coffee", "100.00", alice, Set.of(alice, bob));

        List<SettlementTransactionResponse> settlements = calculator.calculateSettlements(
                List.of(alice, bob, charlie),
                List.of(e)
        );

        assertEquals(1, settlements.size());
        assertEquals(new BigDecimal("50.00"), settlements.get(0).amount());
        assertEquals(2L, settlements.get(0).fromParticipantId());
        assertEquals(1L, settlements.get(0).toParticipantId());
    }

    private ExpenseEntity createExpense(String desc, String amountStr, ParticipantEntity paidBy, Set<ParticipantEntity> participants) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setDescription(desc);
        expense.setAmount(new BigDecimal(amountStr));
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setParticipants(participants);
        return expense;
    }

    private BigDecimal findSettlementAmount(List<SettlementTransactionResponse> settlements, Long fromId, Long toId) {
        return settlements.stream()
                .filter(s -> s.fromParticipantId().equals(fromId) && s.toParticipantId().equals(toId))
                .map(SettlementTransactionResponse::amount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
