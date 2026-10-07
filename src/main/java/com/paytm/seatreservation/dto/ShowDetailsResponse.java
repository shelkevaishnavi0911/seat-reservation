package com.paytm.seatreservation.dto;
import java.util.List;

public record ShowDetailsResponse(

        Long show_id,

        String name,

        Long price_paise,

        Integer per_user_limit,

        Integer total_seats,

        Integer available_seats,

        Integer held_seats,

        Integer confirmed_seats,

        List<SeatDetails> seats

) {

    public record SeatDetails(

            String seat_number,
            String status
    ) {
    }
}