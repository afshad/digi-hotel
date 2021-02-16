*****README*****
Hotel Room Reservation System.

This is a simple gradle application for an in-memory hotel room reservation system.
The application is thread safe. 

Building the app: gradlew clean build

The above command will build the app and execute the unit tests.

Java version: Java 8

Data Structures Used:
1. List to store the hotel rooms.
2. Map of <Key, Value> : <DateTime(as long), List of Bookings for that Date>
Using a map to store the bookings for each day helps us quickly insert and find a booking for a particular date.
3. Map of <Key, Value> : <Customer Unique ID, List of Bookings for that customer>
Using a map to store the bookings for each customer helps us quickly find bookings for a particular customer.

Time Complexity:
Creation of a new Booking is done in time: O(n) where n is the number of days of the booking.
Searching for available rooms for a date is done in constant time O(1).
Searching for bookings for a particular guest is also done in constant time O(1).


Client use:
//The Number of rooms in the hotel is configurable.
		Hotel hotel = new Hotel("Mariott", 10);
		BookingService bookingService = BookingServiceImpl.getInstance();
		Set<Room> availableRoomsOnDate = bookingService.getAvailableRoomsOnDate(hotel, LocalDate.now());
		bookingService.createBooking(hotel, "Afshad", "Dinshaw", "ABCD123", LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(4));
		