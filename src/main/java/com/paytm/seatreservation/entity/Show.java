package com.paytm.seatreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shows", indexes = { @Index(name = "idx_show_created_at", columnList = "created_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "price_of_seat", nullable = false)
	private Long priceOfSeat;

	@Column(name = "per_user_limit", nullable = false)
	@Builder.Default
	private Integer perUserLimit = 4;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
