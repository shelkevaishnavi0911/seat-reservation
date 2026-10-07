package com.paytm.seatreservation.dto;

import java.util.List;

public record ReservationResponse(

		Long reservation_id,

		Long show_id,

		String user_id,

		List<String> seats,

		Long amount_paise,

		String status

) {
}