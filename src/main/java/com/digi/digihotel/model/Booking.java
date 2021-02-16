package com.digi.digihotel.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Booking {
	private final String bookingId;
	private final String guestFirstName;
	private final String guestLastName;
	private final String guestId;
	private final Room room;
	private final LocalDate checkInDate;
	private final LocalDate checkOutDate;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((bookingId == null) ? 0 : bookingId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Booking other = (Booking) obj;
		if (bookingId == null) {
			if (other.bookingId != null) {
				return false;
			}
		} else if (!bookingId.equals(other.bookingId)) {
			return false;
		}
		return true;
	}

}
