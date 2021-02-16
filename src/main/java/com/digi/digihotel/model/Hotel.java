package com.digi.digihotel.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class Hotel {
	private final String name;
	private final int numberOfRooms;
	private final List<Room> rooms;

	public Hotel(String name, int numberOfRooms) {
		this.name = name;
		this.numberOfRooms = numberOfRooms;
		rooms = new ArrayList<Room>();
		for (int i = 1; i < (numberOfRooms + 1); i++) {
			rooms.add(new Room(i));
		}
	}

	public Room findRoom(Integer roomNumber) {
		return rooms.stream().filter(e -> e.getRoomNumber() == roomNumber).findAny().orElse(null);
	}
}
