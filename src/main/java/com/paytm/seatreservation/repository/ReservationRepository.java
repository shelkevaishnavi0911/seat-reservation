package com.paytm.seatreservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paytm.seatreservation.entity.Reservation;
import com.paytm.seatreservation.entity.ReservationStatus;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	Optional<Reservation> findByShowIdAndBookedByUserAndIdempotencyKey(Long showId, String bookedByUser,
			String idempotencyKey);

	long countByShowIdAndBookedByUserAndStatus(Long showId, String bookedByUser, ReservationStatus status);
}
