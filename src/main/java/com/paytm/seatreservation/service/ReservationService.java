package com.paytm.seatreservation.service;

import com.paytm.seatreservation.dto.ReservationResponse;
import com.paytm.seatreservation.dto.ReserveSeatRequest;
import com.paytm.seatreservation.entity.Reservation;
import com.paytm.seatreservation.entity.ReservationStatus;
import com.paytm.seatreservation.entity.Seat;
import com.paytm.seatreservation.entity.SeatStatus;
import com.paytm.seatreservation.entity.Show;
import com.paytm.seatreservation.repository.ReservationRepository;
import com.paytm.seatreservation.repository.SeatRepository;
import com.paytm.seatreservation.repository.ShowRepository;
import com.paytm.seatreservation.security.AuthenticatedUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paytm.seatreservation.util.RequestHashUtil;
import com.paytm.seatreservation.exception.IdempotencyConflictException;
import com.paytm.seatreservation.exception.ReservationAlreadyCancelledException;
import com.paytm.seatreservation.exception.ReservationNotFoundException;

import java.util.List;
import com.paytm.seatreservation.exception.SeatNotAvailableException;
import com.paytm.seatreservation.exception.BookingLimitExceededException;

@Service
public class ReservationService {

	
	    private final ShowRepository showRepository;
	    private final SeatRepository seatRepository;
	    private final ReservationRepository reservationRepository;

	    public ReservationService(
	            ShowRepository showRepository,
	            SeatRepository seatRepository,
	            ReservationRepository reservationRepository) {

	        this.showRepository = showRepository;
	        this.seatRepository = seatRepository;
	        this.reservationRepository = reservationRepository;
	    }

	    @Transactional
	    public ReservationResponse reserve(
	            Long showId,
	            ReserveSeatRequest request) {

	        
	        Authentication authentication =
	                SecurityContextHolder.getContext()
	                        .getAuthentication();

	        AuthenticatedUser authenticatedUser =
	                (AuthenticatedUser) authentication.getPrincipal();

	        String userId = authenticatedUser.userId();

	       
	        Show show = showRepository.findByIdForUpdate(showId)
	                .orElseThrow(() ->
	                        new RuntimeException("Show not found"));

	       
	        List<String> seats = request.seats()
	                .stream()
	                .distinct()
	                .sorted()
	                .toList();

	       
	        String requestHash =
	                RequestHashUtil.generateHash(seats);

	        long alreadyBooked =
	                seatRepository.countByShowIdAndBookedByUserAndStatus(
	                        showId,
	                        userId,
	                        SeatStatus.CONFIRMED
	                );

	        if (alreadyBooked + seats.size() > show.getPerUserLimit()) {
	            throw new BookingLimitExceededException(
	                    "Per-user booking limit exceeded");
	        }

	      
	        var existingReservation =
	                reservationRepository
	                        .findByShowIdAndBookedByUserAndIdempotencyKey(
	                                showId,
	                                userId,
	                                request.idempotency_key());

	        if (existingReservation.isPresent()) {

	            Reservation reservation =
	                    existingReservation.get();

	            if (!reservation.getRequestHash()
	                    .equals(requestHash)) {

	            	throw new IdempotencyConflictException(
	                        "Idempotency key already used with different request");
	            }

	           
	            return buildResponse(reservation);
	        }

	     
	        long totalAmount =
	                show.getPriceOfSeat() * seats.size();

	     
	        Reservation reservation =
	                Reservation.builder()
	                        .show(show)
	                        .bookedByUser(userId)
	                        .totalAmountPaise(totalAmount)
	                        .status(ReservationStatus.CONFIRMED)
	                        .idempotencyKey(
	                                request.idempotency_key())
	                        .requestHash(requestHash)
	                        .build();

	        Reservation savedReservation =
	                reservationRepository.save(reservation);


	        for (String seatNumber : seats) {

	            int updated =
	                    seatRepository.confirmSeat(
	                            showId,
	                            seatNumber,
	                            userId,
	                            savedReservation.getId(),
	                            SeatStatus.CONFIRMED,
	                            SeatStatus.AVAILABLE);

	            if (updated == 0) {

	            	    throw new SeatNotAvailableException(
	            	            "Seat " + seatNumber +
	            	            " is not available");
	            	}
	            }
	        

	        return buildResponse(savedReservation);
	    }

	    private ReservationResponse buildResponse(
	            Reservation reservation) {

	        List<String> seats =
	                seatRepository
	                        .findByReservationIdOrderBySeatNumber(
	                                reservation.getId())
	                        .stream()
	                        .map(Seat::getSeatNumber)
	                        .toList();

	        return new ReservationResponse(
	                reservation.getId(),
	                reservation.getShow().getId(),
	                reservation.getBookedByUser(),
	                seats,
	                reservation.getTotalAmountPaise(),
	                reservation.getStatus().name());
	    }
	    
	    
	    
	    
	    @Transactional
	    public ReservationResponse cancel(Long reservationId) {

	        Authentication authentication =
	                SecurityContextHolder.getContext()
	                        .getAuthentication();

	        AuthenticatedUser authenticatedUser =
	                (AuthenticatedUser) authentication.getPrincipal();

	        String userId = authenticatedUser.userId();

	        Reservation reservation =
	                reservationRepository
	                        .findByIdAndBookedByUser(
	                                reservationId,
	                                userId)
	                        .orElseThrow(() ->
	                                new ReservationNotFoundException(
	                                        "Reservation not found"));

	        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
	            throw new ReservationAlreadyCancelledException(
	                    "Reservation is already cancelled");
	        }

	        List<String> cancelledSeats =
	                seatRepository
	                        .findByReservationIdOrderBySeatNumber(
	                                reservationId)
	                        .stream()
	                        .map(Seat::getSeatNumber)
	                        .toList();

	        reservation.setStatus(ReservationStatus.CANCELLED);

	        seatRepository.releaseSeats(
	                reservationId,
	                SeatStatus.AVAILABLE,
	                SeatStatus.CONFIRMED
	        );

	        reservationRepository.save(reservation);

	        return new ReservationResponse(
	                reservation.getId(),
	                reservation.getShow().getId(),
	                reservation.getBookedByUser(),
	                cancelledSeats,
	                reservation.getTotalAmountPaise(),
	                reservation.getStatus().name()
	        );
	    }
	}

