package com.taxcompliance.exception;

import com.taxcompliance.constant.ErrorCodes;
import com.taxcompliance.dto.response.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessExceptionReturnsConfiguredErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sample");
        BusinessException exception = new BusinessException("SAMPLE_ERROR", "Sample failure", HttpStatus.CONFLICT);

        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals(409, body.status());
        assertEquals("SAMPLE_ERROR", body.errorCode());
        assertEquals("Sample failure", body.message());
        assertEquals("/api/sample", body.path());
        assertEquals(0, body.validationErrors().size());
    }

    @Test
    void handleUnexpectedExceptionDoesNotExposeInternalDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sample");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new IllegalStateException("internal implementation detail"),
                request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertEquals(500, body.status());
        assertEquals(ErrorCodes.INTERNAL_SERVER_ERROR, body.errorCode());
        assertEquals("An unexpected error occurred", body.message());
        assertEquals("/api/sample", body.path());
    }
}
