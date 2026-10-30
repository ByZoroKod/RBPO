package org.example.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiError {
    private final int status;
    private final String message;
    private final Instant timestamp;

    public ApiError(int status, String message, Instant timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}
