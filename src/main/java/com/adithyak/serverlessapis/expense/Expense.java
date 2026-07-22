package com.adithyak.serverlessapis.expense;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;
    private BigDecimal amount;
    private String currency = "USD";
    private ExpenseCategory category;
    private String description;
    private LocalDate date;
    private List<String> tags;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
