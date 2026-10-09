
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	    private static final Logger log =
	            LoggerFactory.getLogger(GlobalExceptionHandler.class);

	    private String getRequestId(HttpServletRequest request) {
	        return (String) request.getAttribute("X-Request-ID");
	    }

	    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
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

	    @ExceptionHandler({
	            MethodArgumentNotValidException.class,
	            HttpMessageNotReadableException.class,
	            ConstraintViolationException.class,
	            MethodArgumentTypeMismatchException.class
	    })
	    public ResponseEntity<ApiErrorResponse> handleBadRequest(
	            Exception ex,
	            HttpServletRequest request) {

	        String message =
	                "Invalid request. Please check the supplied fields.";

	        if (ex instanceof MethodArgumentNotValidException validationEx) {
	            message = validationEx.getBindingResult()
	                    .getFieldErrors()
	                    .stream()
	                    .map(error -> error.getField() + ": "
	                            + error.getDefaultMessage())
	                    .distinct()
	                    .reduce((first, second) ->
	                            first + "; " + second)
	                    .orElse(message);
	        }

	        return buildErrorResponse(
	                request, HttpStatus.BAD_REQUEST, message);
	    }

	    @ExceptionHandler(IdempotencyConflictException.class)
	    public ResponseEntity<ApiErrorResponse> handleIdempotencyConflict(
	            IdempotencyConflictException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.CONFLICT, ex.getMessage());
	    }

	    @ExceptionHandler(SeatNotAvailableException.class)
	    public ResponseEntity<ApiErrorResponse> handleSeatUnavailable(
	            SeatNotAvailableException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.CONFLICT, ex.getMessage());
	    }

	    @ExceptionHandler(BookingLimitExceededException.class)
	    public ResponseEntity<ApiErrorResponse> handleBookingLimitExceeded(
	            BookingLimitExceededException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.CONFLICT, ex.getMessage());
	    }

	    @ExceptionHandler(ReservationAlreadyCancelledException.class)
	    public ResponseEntity<ApiErrorResponse> handleReservationAlreadyCancelled(
	            ReservationAlreadyCancelledException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.CONFLICT, ex.getMessage());
	    }

	    @ExceptionHandler(ReservationNotFoundException.class)
	    public ResponseEntity<ApiErrorResponse> handleReservationNotFound(
	            ReservationNotFoundException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.NOT_FOUND, ex.getMessage());
	    }

	    @ExceptionHandler(ShowNotFoundException.class)
	    public ResponseEntity<ApiErrorResponse> handleShowNotFound(
	            ShowNotFoundException ex,
	            HttpServletRequest request) {

	        return buildErrorResponse(
	                request, HttpStatus.NOT_FOUND, ex.getMessage());
	    }

	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
	            Exception ex,
	            HttpServletRequest request) {

	        log.error(
	                "Unexpected error processing {} {} (requestId={})",
	                request.getMethod(),
	                request.getRequestURI(),
	                getRequestId(request),
	                ex
	        );

	        return buildErrorResponse(
	                request,
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "An unexpected error occurred. Please try again later."
	        );
	    }


}