package com.allobank.splitbill.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ServiceChargeCalculator {

    private final String githubUsername;

    public ServiceChargeCalculator(@Value("${app.github-username:ahmad-user}") String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public int getServiceChargePct() {
        return calculatePct(githubUsername);
    }

    public int calculatePct(String username) {
        if (username == null || username.isBlank()) {
            return 0;
        }
        String lower = username.toLowerCase();
        int sum = 0;
        for (int i = 0; i < lower.length(); i++) {
            sum += lower.charAt(i);
        }
        return Math.abs(sum % 10);
    }

    public BigDecimal calculateServiceChargeAmount(BigDecimal totalExpenses, int serviceChargePct) {
        if (totalExpenses == null || totalExpenses.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal pct = BigDecimal.valueOf(serviceChargePct);
        return totalExpenses.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
