package com.retailer.sku.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "RFC 7807 Problem Details")
public class ProblemDetail {

    @Schema(description = "URI reference identifying the problem type", example = "https://api.retailer.com/problems/not-found")
    private URI type;

    @Schema(description = "Short human-readable summary", example = "SKU Not Found")
    private String title;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Human-readable explanation", example = "SKU with id 550e8400-e29b-41d4-a716-446655440000 was not found")
    private String detail;

    @Schema(description = "URI reference identifying the specific occurrence", example = "/api/v1/skus/550e8400-e29b-41d4-a716-446655440000")
    private URI instance;

    @Schema(description = "Correlation ID for tracing", example = "abc123def456")
    private String traceId;

    @Schema(description = "Timestamp of the error", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "Additional error details")
    private Map<String, Object> errors;
}
