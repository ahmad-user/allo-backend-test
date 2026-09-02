package com.allobank.splitbill.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceChargeCalculatorTest {

    @Test
    @DisplayName("Calculate service charge pct for example username johndoe47 should be 0")
    void testJohnDoe47() {
        ServiceChargeCalculator calculator = new ServiceChargeCalculator("johndoe47");
        int pct = calculator.getServiceChargePct();
        assertEquals(0, pct);
    }

    @ParameterizedTest
    @CsvSource({
            "johndoe47, 0",
            "JOHNDOE47, 0",
            "alice, 0",     // 97+108+105+99+101 = 510 -> 0
            "bob, 7",       // 98+111+98 = 307 -> 7
            "charlie, 8"    // 99+104+97+114+108+105+101 = 728 -> 8
    })
    void testVariousUsernames(String username, int expectedPct) {
        ServiceChargeCalculator calculator = new ServiceChargeCalculator(username);
        int pct = calculator.getServiceChargePct();
        assertEquals(expectedPct, pct);
    }

    @Test
    @DisplayName("Service charge amount calculation with BigDecimal precision")
    void testServiceChargeAmount() {
        ServiceChargeCalculator calculator = new ServiceChargeCalculator("bob");
        // bob -> 7%
        BigDecimal totalExpenses = new BigDecimal("300000.00");
        BigDecimal amount = calculator.calculateServiceChargeAmount(totalExpenses, 7);
        assertEquals(new BigDecimal("21000.00"), amount);
    }

    @Test
    @DisplayName("Service charge amount calculation for zero total expenses")
    void testServiceChargeAmountZero() {
        ServiceChargeCalculator calculator = new ServiceChargeCalculator("johndoe47");
        BigDecimal amount = calculator.calculateServiceChargeAmount(BigDecimal.ZERO, 0);
        assertEquals(new BigDecimal("0.00"), amount);
    }
}
