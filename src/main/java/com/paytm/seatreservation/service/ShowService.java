package com.paytm.seatreservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.paytm.seatreservation.dto.CreateShowRequest;
import com.paytm.seatreservation.dto.CreateShowResponse;
import com.paytm.seatreservation.dto.ShowDetailsResponse;
import com.paytm.seatreservation.entity.Seat;
import com.paytm.seatreservation.entity.SeatStatus;
import com.paytm.seatreservation.entity.Show;
import com.paytm.seatreservation.repository.SeatRepository;
import com.paytm.seatreservation.repository.ShowRepository;

import jakarta.transaction.Transactional;

@Service
public class ShowService {

	private final ShowRepository showRepository;
	private final SeatRepository seatRepository;

	public ShowService(ShowRepository showRepository, SeatRepository seatRepository) {
		this.showRepository = showRepository;
		this.seatRepository = seatRepository;
	}

	@Transactional
	public CreateShowResponse createShow(CreateShowRequest request) {

		Show show = Show.builder().name(request.name()).priceOfSeat(request.price_paise())
				.perUserLimit(request.per_user_limit() != null ? request.per_user_limit() : 4).active(true).build();

		Show savedShow = showRepository.save(show);

		List<Seat> seats = request.seats().stream().distinct().map(seatNumber -> Seat.builder().show(savedShow)
				.seatNumber(seatNumber).status(SeatStatus.AVAILABLE).build()).toList();

		seatRepository.saveAll(seats);

		return new CreateShowResponse(savedShow.getId(), savedShow.getName(), seats.size(), savedShow.getPriceOfSeat(),
				savedShow.getPerUserLimit());
	}

	
	
	@Transactional
	public ShowDetailsResponse getShow(Long showId) {

	    Show show = showRepository.findById(showId)
	            .orElseThrow(() ->
	                    new RuntimeException("Show not found"));

	    List<Seat> seats =
	            seatRepository.findByShowIdOrderBySeatNumber(showId);

	    int availableSeats = 0;
	    int heldSeats = 0;
	    int confirmedSeats = 0;

	    List<ShowDetailsResponse.SeatDetails> seatDetails =
	            seats.stream()
	                    .map(seat -> {
	                        return new ShowDetailsResponse.SeatDetails(
	                                seat.getSeatNumber(),
	                                seat.getStatus().name()
	                        );
	                    })
	                    .toList();

	    for (Seat seat : seats) {
	        if (seat.getStatus() == SeatStatus.AVAILABLE) {
	            availableSeats++;
	        } else if (seat.getStatus() == SeatStatus.HELD) {
	            heldSeats++;
	        } else if (seat.getStatus() == SeatStatus.CONFIRMED) {
	            confirmedSeats++;
	        }
	    }

	    return new ShowDetailsResponse(
	            show.getId(),
	            show.getName(),
	            show.getPriceOfSeat(),
	            show.getPerUserLimit(),
	            seats.size(),
	            availableSeats,
	            heldSeats,
	            confirmedSeats,
	            seatDetails
	    );
	}
}
