package com.taxcompliance.exception;

import com.taxcompliance.constant.ErrorCodes;
import com.taxcompliance.dto.response.ApiErrorResponse;
import com.taxcompliance.dto.response.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        HttpStatus status = exception.getHttpStatus();
        return ResponseEntity.status(status)
                .body(buildResponse(status, exception.getErrorCode(), exception.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCodes.RESOURCE_NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<FieldValidationError> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCodes.VALIDATION_ERROR, "Request validation failed", request.getRequestURI(), validationErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<FieldValidationError> validationErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCodes.VALIDATION_ERROR, "Request validation failed", request.getRequestURI(), validationErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCodes.MALFORMED_REQUEST, "Malformed request body", request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        LOGGER.error("Unexpected application error occurred: {}", exception.getClass().getName());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCodes.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI(), List.of()));
    }

    private ApiErrorResponse buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            String path,
            List<FieldValidationError> validationErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                path,
                validationErrors);
    }
}
