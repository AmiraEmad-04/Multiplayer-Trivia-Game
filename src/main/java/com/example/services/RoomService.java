package com.example.services;

import com.example.objects.Room;
import com.example.objects.User;
import java.util.ArrayList;
import java.util.Scanner;

public class RoomManagerService {

    private ArrayList<Room> rooms;
    private Scanner scanner;

    public RoomManagerService() {
        rooms = new ArrayList<>();
        scanner = new Scanner(System.in);
    }

    // ================= CREATE ROOM
    public void createRoom(User user) {

        System.out.print("Enter room name: ");
        String name = scanner.nextLine();

        Room room = new Room(name);
        room.addPlayer(user);

        rooms.add(room);

        System.out.println("Room created successfully.");
    }

    // ================= JOIN RANDOM ROOM
    public void joinRandomRoom(User user) {

        if (rooms.isEmpty()) {

            System.out.println("No rooms available. Creating new room.");

            createRoom(user);
            return;
        }

        Room room = rooms.get(0); // simple random simulation

        room.addPlayer(user);

        System.out.println(user.getUsername() + " joined room " + room.getRoomName());
    }

    // ================= SHOW ROOMS
    public void showRooms() {

        if (rooms.isEmpty()) {

            System.out.println("No active rooms.");
            return;
        }

        for (Room r : rooms) {
            r.showPlayers();
        }
    }

}
