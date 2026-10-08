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
	
	private String getRequestId(HttpServletRequest request) {
	    return (String) request.getAttribute("X-Request-ID");
	}

	@ExceptionHandler(IdempotencyConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex,
			HttpServletRequest request) {

		ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
				ex.getMessage(), request.getRequestURI(), getRequestId(request));

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
	            getRequestId(request)
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
	            getRequestId(request)
	    );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(response);
	}
	
	
	
	@ExceptionHandler(ReservationAlreadyCancelledException.class)
	public ResponseEntity<ApiErrorResponse> handleReservationAlreadyCancelled(
	        ReservationAlreadyCancelledException ex,
	        HttpServletRequest request) {

	    ApiErrorResponse response = new ApiErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.CONFLICT.value(),
	            "Conflict",
	            ex.getMessage(),
	            request.getRequestURI(),
	            getRequestId(request)
	    );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(response);
	}
	
	
	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleReservationNotFound(
	        ReservationNotFoundException ex,
	        HttpServletRequest request) {

	    ApiErrorResponse response = new ApiErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.NOT_FOUND.value(),
	            "Not Found",
	            ex.getMessage(),
	            request.getRequestURI(),
	            getRequestId(request)
	    );

	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(response);
	}
}
