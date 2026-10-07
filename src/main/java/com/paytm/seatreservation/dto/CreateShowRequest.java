package com.paytm.seatreservation.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateShowRequest(

        @NotBlank(message = "Show name is required")
        String name,

        @NotEmpty(message = "Seats cannot be empty")
        List<@NotBlank(message = "Seat number cannot be blank") String> seats,

        @Positive(message = "Price must be greater than zero")
        Long price_paise,

        @Min(value = 1, message = "Per-user limit must be at least 1")
        Integer per_user_limit

) {
}
