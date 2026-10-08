package com.paytm.seatreservation.repository;


	import com.paytm.seatreservation.entity.Seat;
	import com.paytm.seatreservation.entity.SeatStatus;
	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.data.jpa.repository.Modifying;
	import org.springframework.data.jpa.repository.Query;
	import org.springframework.data.repository.query.Param;

	import java.util.List;

	public interface SeatRepository extends JpaRepository<Seat, Long> {

	    List<Seat> findByShowIdOrderBySeatNumber(Long showId);

	    long countByShowIdAndStatus(
	            Long showId,
	            SeatStatus status
	    );

	    @Modifying
	    @Query("""
	        UPDATE Seat s
	        SET s.status = :newStatus,
	            s.bookedByUser = :bookedByUser,
	            s.reservationId = :reservationId
	        WHERE s.show.id = :showId
	          AND s.seatNumber = :seatNumber
	          AND s.status = :availableStatus
	        """)
	    int confirmSeat(
	            @Param("showId") Long showId,
	            @Param("seatNumber") String seatNumber,
	            @Param("bookedByUser") String bookedByUser,
	            @Param("reservationId") Long reservationId,
	            @Param("newStatus") SeatStatus newStatus,
	            @Param("availableStatus") SeatStatus availableStatus
	    );
	    
	    List<Seat> findByReservationIdOrderBySeatNumber(Long reservationId);
	    
	    long countByShowIdAndBookedByUserAndStatus(
	            Long showId,
	            String bookedByUser,
	            SeatStatus status
	    );
	}


