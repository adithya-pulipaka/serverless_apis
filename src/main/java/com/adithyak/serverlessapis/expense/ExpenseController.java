package com.adithyak.serverlessapis.expense;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Expense tracking")
@SecurityRequirement(name = "X-API-Key")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all expenses", description = "Optionally filter by category and/or date range (ISO format: yyyy-MM-dd)")
    public List<Expense> list(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findAll(category, from, to);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get expense totals grouped by category")
    public List<ExpenseSummary> summary() {
        return service.summarize();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an expense")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Expense create(@Valid @RequestBody ExpenseRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Expense getById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense")
    public Expense update(@PathVariable String id, @Valid @RequestBody ExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an expense")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
