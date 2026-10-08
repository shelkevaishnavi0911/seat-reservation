package com.paytm.seatreservation.exception;

import com.paytm.seatreservation.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IdempotencyConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex,
			HttpServletRequest request) {

		ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
				ex.getMessage(), request.getRequestURI(), request.getHeader("X-Request-ID"));

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}
	
	
	@ExceptionHandler(SeatNotAvailableException.class)
	public ResponseEntity<ApiErrorResponse> handleSeatUnavailable(
			SeatNotAvailableException ex,
	        HttpServletRequest request) {
	    ApiErrorResponse response = new ApiErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.CONFLICT.value(),
	            "Conflict",
	            ex.getMessage(),
	            request.getRequestURI(),
	            request.getHeader("X-Request-ID")
	    );
	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(response);
	}
	
	@ExceptionHandler(BookingLimitExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleBookingLimitExceeded(
	        BookingLimitExceededException ex,
	        HttpServletRequest request) {

	    ApiErrorResponse response = new ApiErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.CONFLICT.value(),
	            "Conflict",
	            ex.getMessage(),
	            request.getRequestURI(),
	            request.getHeader("X-Request-ID")
	    );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(response);
	}
}
