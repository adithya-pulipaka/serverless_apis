package com.adithyak.serverlessapis.expense;

import com.adithyak.serverlessapis.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> findAll(ExpenseCategory category, LocalDate from, LocalDate to) {
        if (category != null && from != null && to != null) {
            return repository.findByCategoryAndDateBetween(category, from, to);
        }
        if (from != null && to != null) return repository.findByDateBetween(from, to);
        if (category != null) return repository.findByCategory(category);
        return repository.findAll();
    }

    public Expense findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    public Expense create(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency() != null ? request.currency() : "USD");
        expense.setCategory(request.category());
        expense.setDescription(request.description());
        expense.setDate(request.date());
        expense.setTags(request.tags());
        return repository.save(expense);
    }

    public Expense update(String id, ExpenseRequest request) {
        Expense expense = findById(id);
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency() != null ? request.currency() : "USD");
        expense.setCategory(request.category());
        expense.setDescription(request.description());
        expense.setDate(request.date());
        expense.setTags(request.tags());
        return repository.save(expense);
    }

    public void delete(String id) {
        findById(id);
        repository.deleteById(id);
    }

    /** Groups all expenses by category and sums totals — used for the dashboard summary view. */
    public List<ExpenseSummary> summarize() {
        Map<ExpenseCategory, List<Expense>> grouped = repository.findAll().stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        return grouped.entrySet().stream()
                .map(entry -> new ExpenseSummary(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparing(s -> s.category().name()))
                .collect(Collectors.toList());
    }
}
