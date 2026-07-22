package com.adithyak.serverlessapis.todo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todos")
@Tag(name = "Todos", description = "Todo list management")
@SecurityRequirement(name = "X-API-Key")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all todos", description = "Optionally filter by status and/or priority")
    public List<Todo> list(
            @RequestParam(required = false) TodoStatus status,
            @RequestParam(required = false) TodoPriority priority) {
        return service.findAll(status, priority);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a todo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Todo create(@Valid @RequestBody TodoRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Todo getById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a todo")
    public Todo update(@PathVariable String id, @Valid @RequestBody TodoRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update todo status only")
    public Todo updateStatus(@PathVariable String id, @RequestParam TodoStatus status) {
        return service.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a todo")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
