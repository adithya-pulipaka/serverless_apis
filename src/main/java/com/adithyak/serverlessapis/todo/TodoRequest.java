package com.adithyak.serverlessapis.todo;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TodoRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        TodoStatus status,
        TodoPriority priority,
        LocalDate dueDate
) {}
