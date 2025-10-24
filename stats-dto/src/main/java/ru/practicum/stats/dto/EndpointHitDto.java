package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/** Represents a record of a request to a specific service endpoint. Used in POST /hit. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitDto {

    /** Database record ID (read-only, ignored on creation). */
    private Long id;

    /** Application or service name. */
    @NotBlank(message = "app must not be blank")
    private String app;

    /** Target URI of the request. */
    @NotBlank(message = "uri must not be blank")
    private String uri;

    /** Client IP address. */
    @NotBlank(message = "ip must not be blank")
    private String ip;

    /** Request time in format yyyy-MM-dd HH:mm:ss. */
    @NotNull(message = "timestamp must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}