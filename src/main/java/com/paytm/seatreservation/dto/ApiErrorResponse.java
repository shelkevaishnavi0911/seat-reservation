package com.paytm.seatreservation.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(

		LocalDateTime timestamp,

		int status,

		String error,

		String message,

		String path,

		String request_id

) {
}
