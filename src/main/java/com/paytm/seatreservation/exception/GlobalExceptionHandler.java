
package com.paytm.seatreservation.exception;

import com.paytm.seatreservation.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getRequestId(HttpServletRequest request) {
        return (String) request.getAttribute("X-Request-ID");
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception ex,
            HttpServletRequest request,
            HttpStatus status,
            String message) {

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                getRequestId(request)
        );

        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception ex,
            HttpServletRequest request,
            HttpStatus status) {
        return buildErrorResponse(ex, request, status, ex.getMessage());
    }

    
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        ConstraintViolationException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request) {

        String message = "Invalid request. Please check the supplied fields.";

        if (ex instanceof MethodArgumentNotValidException validationEx) {
            message = validationEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": "
                            + error.getDefaultMessage())
                    .distinct()
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(message);
        }

        return buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, message);
    }

   
    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotencyConflict(
            IdempotencyConflictException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ApiErrorResponse> handleSeatUnavailable(
            SeatNotAvailableException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    
    @ExceptionHandler(BookingLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingLimitExceeded(
            BookingLimitExceededException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    
    @ExceptionHandler(ReservationAlreadyCancelledException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationAlreadyCancelled(
            ReservationAlreadyCancelledException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

  
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationNotFound(
            ReservationNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {

       
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
                .error("Unexpected error processing request {}",
                        request.getRequestURI(), ex);

        return buildErrorResponse(
                ex,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
    }
}