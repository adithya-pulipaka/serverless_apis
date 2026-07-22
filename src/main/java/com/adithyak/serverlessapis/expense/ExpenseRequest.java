package com.adithyak.serverlessapis.expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequest(
        @NotNull(message = "amount is required") @Positive(message = "amount must be positive") BigDecimal amount,
        String currency,
        @NotNull(message = "category is required") ExpenseCategory category,
        String description,
        @NotNull(message = "date is required") LocalDate date,
        List<String> tags
) {}
