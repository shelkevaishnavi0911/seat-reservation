package com.paytm.seatreservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
public record ReserveSeatRequest(

        @NotEmpty(message = "Seats cannot be empty")
        List<@NotBlank(message = "Seat number cannot be blank") String> seats,

        @NotBlank(message = "Idempotency key is required")
        String idempotency_key

) {
}
