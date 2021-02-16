package com.digi.digihotel.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.digi.digihotel.model.Booking;
import com.digi.digihotel.model.Hotel;
import com.digi.digihotel.model.Room;

public class BookingServiceTest {

	@Before
	public void init() {
		BookingService bookingService = BookingServiceImpl.getInstance();
		bookingService.reset();
	}

	@Test
	public void testCreateBooking() {
		Hotel hotel = new Hotel("Mariott", 10);
		BookingService bookingService = BookingServiceImpl.getInstance();
		Set<Room> availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now());
		assertNull("There should not be any booking.", availableRoomsOnDate);
		// Book a room for 3 days
		bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(4));
		// ALSO VERIFY METHOD getAvailableRoomsOnDate
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(1));
		assertTrue("There should be a booking for this date.", availableRoomsOnDate.size() == 9);
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(2));
		assertTrue("There should be a booking for this date.", availableRoomsOnDate.size() == 9);
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(3));
		assertTrue("There should be a booking for this date.", availableRoomsOnDate.size() == 9);
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(4));
		assertNull("There should not be any booking.", availableRoomsOnDate);
	}

	@Test
	public void testCreateBookingIfHotelFull() {
		Hotel hotel = new Hotel("Mariott", 2);
		BookingService bookingService = BookingServiceImpl.getInstance();
		Set<Room> availableRoomsOnDate;
		// Book a room for 1 day
		bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2));
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(1));
		assertTrue("There should be a booking for this date.", availableRoomsOnDate.size() == 1);
		// Book another room for the same day
		bookingService.createBooking(hotel, "John", "Shaw", "XYZ123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(2));
		availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now().plusDays(1));
		assertTrue("There should be 2 bookings for this date.", availableRoomsOnDate.size() == 0);

		// Should not be able to make another booking for the same day
		assertNull("Should not be able to make another booking for this date.", bookingService.createBooking(hotel,
				"Brad", "Kent", "XYZRR123", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));

	}

	@Test
	public void testGetBookingsForGuest() {
		Hotel hotel = new Hotel("Mariott", 10);
		BookingService bookingService = BookingServiceImpl.getInstance();
		// Book a room for 2 days for guest ABCD123
		bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(3));
		// Book another room for the same guest for 3 more days
		bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123", LocalDate.now().plusDays(4),
				LocalDate.now().plusDays(7));

		// Book a room for 2 days for another guest XYZ123
		bookingService.createBooking(hotel, "John", "Wilbert", "XYZ123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(3));
		// Book another room for the same guest for 4 more days
		bookingService.createBooking(hotel, "John", "Wilbert", "XYZ123", LocalDate.now().plusDays(4),
				LocalDate.now().plusDays(8));

		// Verify that there are 2 bookings in total for guest ABCD123.
		assertEquals("There should be 2 bookings for this guest.", bookingService.getBookingsForGuest("ABCD123").size(),
				2);
		// Verify that there are 2 bookings in total for guest XYZ123.
		assertEquals("There should be 2 bookings for this guest.", bookingService.getBookingsForGuest("XYZ123").size(),
				2);
	}

	@Test
	public void testConcurrentBookingCreation() throws InterruptedException {
		Hotel hotel = new Hotel("Mariott", 10);
		BookingService bookingService = BookingServiceImpl.getInstance();

		Thread thread1 = new Thread() {
			@Override
			public void run() {
				Set<Room> availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now());
				assertNull("There should not be any booking.", availableRoomsOnDate);
				// Book a room for 3 days
				Booking booking = bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123",
						LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
				assertNotNull("Booking should not be null", booking);
				// Cannot verify output of getAvailableRoomsOnDate since
				// multiple threads booking on same date.
				// Verify that there is 1 booking in total for guest ABCD123.
				assertEquals("There should be 1 booking for this guest.",
						bookingService.getBookingsForGuest("ABCD123").size(), 1);

			}
		};

		Thread thread2 = new Thread() {
			@Override
			public void run() {
				Set<Room> availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now());
				assertNull("There should not be any booking.", availableRoomsOnDate);
				// Book a room for 3 days
				Booking booking = bookingService.createBooking(hotel, "John", "Shaw", "XYZ123",
						LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
				assertNotNull("Booking should not be null", booking);
				// Cannot verify output of getAvailableRoomsOnDate since
				// multiple threads booking on same date.
				// Verify that there is 1 booking in total for guest XYZ123.
				assertEquals("There should be 1 booking for this guest.",
						bookingService.getBookingsForGuest("XYZ123").size(), 1);

			}
		};

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();
	}
}
