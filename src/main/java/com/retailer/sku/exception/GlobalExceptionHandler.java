package com.retailer.sku.exception;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URI = "https://api.retailer.com/problems/";

    @ExceptionHandler(SkuNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSkuNotFoundException(
            SkuNotFoundException ex, HttpServletRequest request) {

        log.warn("SKU not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(PROBLEM_BASE_URI + "not-found"))
                .title("SKU Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateSkuException(
            DuplicateSkuException ex, HttpServletRequest request) {

        log.warn("Duplicate SKU: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(PROBLEM_BASE_URI + "duplicate"))
                .title("Duplicate SKU")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(PROBLEM_BASE_URI + "validation-error"))
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more fields failed validation")
                .instance(URI.create(request.getRequestURI()))
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Invalid argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(PROBLEM_BASE_URI + "invalid-argument"))
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(PROBLEM_BASE_URI + "internal-error"))
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .instance(URI.create(request.getRequestURI()))
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String getTraceId() {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
            return currentSpan.getSpanContext().getTraceId();
        }
        return null;
    }
}
