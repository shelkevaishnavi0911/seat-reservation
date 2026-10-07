package com.paytm.seatreservation.entity;
import lombok.*;
import jakarta.persistence.*;


@Entity
@Table(
		name = "seats",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "unique_show_seat",
						columnNames = {"show_id", "seat_number"}
						)
		},
		indexes = {
				@Index(name = "index_show_id", columnList = "show_id"),
				@Index(name = "index_show_status", columnList = "show_id, status")
		}
		)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "show_id", nullable = false)
	private Show show;

	@Column(name = "seat_number", nullable = false, length = 20)
	private String seatNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private SeatStatus status = SeatStatus.AVAILABLE;

	@Column(name = "booked_by_user")
	private String bookedByUser;

	@Column(name = "reservation_id")
	private Long reservationId;
}

