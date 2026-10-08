package com.paytm.seatreservation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public final class RequestHashUtil {

	private RequestHashUtil() {
	}

	public static String generateHash(List<String> seats) {

		String normalizedSeats = seats.stream().distinct().sorted().reduce((a, b) -> a + "," + b).orElse("");

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(normalizedSeats.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder();

			for (byte b : hash) {
				hexString.append(String.format("%02x", b));
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}
}
