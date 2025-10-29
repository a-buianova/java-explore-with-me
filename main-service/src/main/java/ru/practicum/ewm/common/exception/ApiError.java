package ru.practicum.ewm.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Standard API error payload aligned with Practicum spec.
 * Fields: status (enum), reason (canonical phrase), message, timestamp.
 */
@Value
@Builder
public class ApiError {
    String status;
    String reason;
    String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
}