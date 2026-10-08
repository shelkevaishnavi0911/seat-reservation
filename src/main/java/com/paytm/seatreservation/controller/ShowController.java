package com.paytm.seatreservation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paytm.seatreservation.dto.CreateShowRequest;
import com.paytm.seatreservation.dto.CreateShowResponse;
import com.paytm.seatreservation.dto.ReservationResponse;
import com.paytm.seatreservation.dto.ReserveSeatRequest;
import com.paytm.seatreservation.dto.ShowDetailsResponse;
import com.paytm.seatreservation.service.ReservationService;
import com.paytm.seatreservation.service.ShowService;

import jakarta.validation.Valid;

@RestController

	@RequestMapping("/shows")
	public class ShowController {

	private final ShowService showService;    
	    
	    public ShowController(ShowService showService) {
	        this.showService = showService;
	    }
	    
	    @PostMapping
	    public ResponseEntity<CreateShowResponse> createShow(
	            @Valid @RequestBody CreateShowRequest request
	    ) {

	        CreateShowResponse response =
	                showService.createShow(request);

	        return ResponseEntity
	                .status(HttpStatus.CREATED)
	                .body(response);
	    }
	    
	    @GetMapping("/{showId}")
	    public ResponseEntity<ShowDetailsResponse> getShow(
	            @PathVariable Long showId
	    ) {
	        ShowDetailsResponse response =
	                showService.getShow(showId);

	        return ResponseEntity.ok(response);
	    }
	}


