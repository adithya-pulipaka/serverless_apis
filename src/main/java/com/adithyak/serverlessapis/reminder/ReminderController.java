package com.adithyak.serverlessapis.reminder;

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
@RequestMapping("/api/v1/reminders")
@Tag(name = "Reminders", description = "Reminder management")
@SecurityRequirement(name = "X-API-Key")
public class ReminderController {

    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all reminders", description = "Optionally filter by status")
    public List<Reminder> list(@RequestParam(required = false) ReminderStatus status) {
        return service.findAll(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a reminder")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Reminder create(@Valid @RequestBody ReminderRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a reminder by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public Reminder getById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reminder")
    public Reminder update(@PathVariable String id, @Valid @RequestBody ReminderRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss a reminder")
    public Reminder dismiss(@PathVariable String id) {
        return service.dismiss(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a reminder")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
