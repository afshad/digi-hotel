package com.digi.digihotel.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.digi.digihotel.model.Booking;
import com.digi.digihotel.model.Hotel;
import com.digi.digihotel.model.Room;

public interface BookingService {
	public Booking createBooking(Hotel hotel, String firstName, String lastName, String guestId, LocalDate checkInDate,
			LocalDate checkOutDate);

	public Set<Room> getAvailableRoomsOnDate(Hotel hotel, LocalDate localDate);

	public List<Booking> getBookingsForGuest(String guestId);

	public void reset();

}
