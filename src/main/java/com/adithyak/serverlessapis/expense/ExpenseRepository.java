package com.adithyak.serverlessapis.expense;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByCategory(ExpenseCategory category);
    List<Expense> findByDateBetween(LocalDate from, LocalDate to);
    List<Expense> findByCategoryAndDateBetween(ExpenseCategory category, LocalDate from, LocalDate to);
}
