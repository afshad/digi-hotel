package com.digi.digihotel.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.digi.digihotel.model.Booking;
import com.digi.digihotel.model.Hotel;
import com.digi.digihotel.model.Room;
import com.digi.digihotel.utils.Utils;

import lombok.NonNull;

/**
 * This class is responsible for all the booking operations including creation
 * and search.
 *
 * @author afshad
 *
 */
public final class BookingServiceImpl implements BookingService {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BookingServiceImpl.class);

	private static BookingServiceImpl INSTANCE;
	private static final ZoneId ZONE_ID = ZoneId.systemDefault();
	private final ConcurrentHashMap<Long, List<Booking>> bookingMap;
	private final ConcurrentHashMap<String, List<Booking>> customerSearchMap;

	private BookingServiceImpl() {
		bookingMap = new ConcurrentHashMap<Long, List<Booking>>();
		customerSearchMap = new ConcurrentHashMap<String, List<Booking>>();
	}

	public static BookingServiceImpl getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BookingServiceImpl();
		}

		return INSTANCE;
	}

	/**
	 * Create a booking for a room.
	 *
	 * @param hotel
	 *            The hotel the room belongs to.
	 * @param firstName
	 *            The first name of the customer.
	 * @param lastName
	 *            The last name of the customer.
	 * @param guestId
	 *            The ID of the guest. A String.
	 * @param checkInDate
	 *            The start date of the booking.
	 * @param checkOutDate
	 *            The end date of the booking.
	 * @return the Booking created for the given parameters. null is returned if
	 *         there was no availability for the given dates and a booking could
	 *         not be created.
	 */
	@Override
	public synchronized Booking createBooking(@NonNull Hotel hotel, String firstName, String lastName, String guestId,
			LocalDate checkInDate, LocalDate checkOutDate) {
		if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName) || StringUtils.isEmpty(guestId)
				|| checkOutDate.isBefore(LocalDate.now(ZONE_ID)) || checkOutDate.isBefore(checkInDate)
				|| checkOutDate.isEqual(checkInDate)) {
			// LOGGER.error("Invalid arguments.");
			throw new IllegalArgumentException();
		}

		LOGGER.info("Create a booking from {} to {} ",
				checkInDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
				checkOutDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

		Period period = Period.between(checkInDate, checkOutDate);
		Booking booking = null;
		Set<Integer> commonAvailableRooms = IntStream.rangeClosed(1, hotel.getNumberOfRooms()).boxed()
				.collect(Collectors.toSet());
		// Find the available rooms for each date of the booking
		for (int i = 0; i < period.getDays(); i++) {
			long epoch = checkInDate.plusDays(i).atStartOfDay(ZONE_ID).toEpochSecond();
			List<Booking> list = bookingMap.get(epoch);

			Set<Integer> bookedRooms = list == null ? Collections.emptySet()
					: list.stream().map(e -> e.getRoom().getRoomNumber()).collect(Collectors.toSet());
			Set<Integer> hotelRooms = Utils.getSetFromClosedRange(hotel.getNumberOfRooms());
			// Get the available rooms by getting the complement.
			hotelRooms.removeAll(bookedRooms);
			// We want to find the common available rooms across all requested
			// booking dates.
			commonAvailableRooms.retainAll(hotelRooms);
		}
		if (commonAvailableRooms.size() > 0) {
			booking = Booking.builder().bookingId(UUID.randomUUID().toString()).guestFirstName(firstName)
					.guestLastName(lastName).guestId(guestId)
					.room(hotel.findRoom((new TreeSet<Integer>(commonAvailableRooms)).first())).checkInDate(checkInDate)
					.checkOutDate(checkOutDate).build();
			// Insert entries into the booking map for each date.
			for (int i = 0; i < period.getDays(); i++) {
				long epoch = checkInDate.plusDays(i).atStartOfDay(ZONE_ID).toEpochSecond();
				addToBookingMap(epoch, booking);
				addToCustomerSearchMap(guestId, booking);
			}
		}
		if (booking != null) {
			LOGGER.info("Booking created successfuly {}.", booking);
		} else {
			LOGGER.info("Could not create a booking for the given dates. No availability.");
		}
		return booking;
	}

	/**
	 * Get the available rooms for a date.
	 *
	 * @param hotel
	 *            The hotel the rooms belong to.
	 * @param localDate
	 *            The date to check to get the available rooms for.
	 * @return A Set of rooms representing the available rooms. Note: Null is
	 *         returned if there are no bookings for this date. Which means all
	 *         rooms are available for that date. An empty set is returned if
	 *         there are no available rooms for that date.
	 */
	@Override
	public synchronized Set<Room> getAvailableRoomsOnDate(Hotel hotel, LocalDate localDate) {
		List<Booking> list = bookingMap.get(localDate.atStartOfDay(ZONE_ID).toEpochSecond());
		if (list == null) {
			return null;
		}
		Set<Integer> bookedRooms = list.stream().map(e -> e.getRoom().getRoomNumber()).collect(Collectors.toSet());
		Set<Integer> hotelRooms = Utils.getSetFromClosedRange(hotel.getNumberOfRooms());
		// Get the available rooms by getting the complement.
		hotelRooms.removeAll(bookedRooms);
		Set<Room> availableHotelRooms = new HashSet<Room>();
		for (Room i : hotel.getRooms()) {
			if (hotelRooms.contains(i.getRoomNumber())) {
				availableHotelRooms.add(i);
			}
		}
		return availableHotelRooms;
	}

	/**
	 * Get the bookings for a particular guest.
	 *
	 * @param guestId.
	 *            The Id of the guest to search for.
	 * @return list of bookings for the guest.
	 *
	 */
	@Override
	public List<Booking> getBookingsForGuest(String guestId) {
		return customerSearchMap.get(guestId).stream().distinct().collect(Collectors.toList());
	}

	/**
	 * Clear all the bookings in the system.
	 */
	@Override
	public void reset() {
		bookingMap.clear();
		customerSearchMap.clear();
	}

	private void addToCustomerSearchMap(String guestId, Booking booking) {
		if (customerSearchMap.get(guestId) != null) {
			customerSearchMap.get(guestId).add(booking);
		} else {
			customerSearchMap.put(guestId, new ArrayList<Booking>(Arrays.asList(booking)));
		}
	}

	private void addToBookingMap(long epoch, Booking booking) {
		if (bookingMap.get(epoch) != null) {
			bookingMap.get(epoch).add(booking);
		} else {
			bookingMap.put(epoch, new ArrayList<Booking>(Arrays.asList(booking)));
		}
	}

}
