package com.adithyak.serverlessapis.reminder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReminderRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        @NotNull(message = "remindAt is required") Instant remindAt,
        RecurPattern recurPattern
) {}
