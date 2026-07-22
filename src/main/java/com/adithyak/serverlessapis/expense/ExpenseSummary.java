package com.adithyak.serverlessapis.expense;

import java.math.BigDecimal;

public record ExpenseSummary(
        ExpenseCategory category,
        BigDecimal total,
        long count
) {}
