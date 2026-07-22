package com.adithyak.serverlessapis.shared.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path
) {}
