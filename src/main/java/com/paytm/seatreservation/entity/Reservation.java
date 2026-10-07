package com.paytm.seatreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", uniqueConstraints = {
		@UniqueConstraint(name = "unique_show_user_idempotency", columnNames = { "show_id", "booked_by_user",
				"idempotency_key" }) }, indexes = { @Index(name = "index_reservation_show", columnList = "show_id"),
						@Index(name = "index_reservation_user", columnList = "booked_by_user") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "show_id", nullable = false)
	private Show show;

	@Column(name = "booked_by_user", nullable = false, length = 100)
	private String bookedByUser;

	@Column(name = "total_amount_paise", nullable = false)
	private Long totalAmountPaise;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ReservationStatus status;

	@Column(name = "idempotency_key", nullable = false, length = 100)
	private String idempotencyKey;

	@Column(name = "request_hash", nullable = false, length = 64)
	private String requestHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void setCreatedAt() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
