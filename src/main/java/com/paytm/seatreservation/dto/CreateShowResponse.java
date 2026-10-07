package com.paytm.seatreservation.dto;



public record CreateShowResponse(

        Long show_id,
        String name,
        int total_seats,
        Long price_paise,
        Integer per_user_limit

) {
}