package com.paytm.seatreservation.controller;

import com.paytm.seatreservation.dto.ReservationResponse;
import com.paytm.seatreservation.dto.ReserveSeatRequest;
import com.paytm.seatreservation.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shows")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	
	  @GetMapping("/{showId}/reservation-test")
	    public String reservationTest(@PathVariable Long showId) {
	        return "Reservation Controller Working";
	    }
	  
	@PostMapping("/{showId}/reserve")
	public ResponseEntity<ReservationResponse> reserve(@PathVariable Long showId,
			@Valid @RequestBody ReserveSeatRequest request) {

		ReservationResponse response = reservationService.reserve(showId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
