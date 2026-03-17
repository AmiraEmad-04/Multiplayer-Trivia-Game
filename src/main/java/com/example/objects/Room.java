package com.example.objects;

import java.util.ArrayList;

class Room {

    private String roomName;
    private ArrayList<User> players;

    public Room(String roomName) {
        this.roomName = roomName;
        players = new ArrayList<>();
    }

    public void addPlayer(User user) {
        players.add(user);
    }

    public int size() {
        return players.size();
    }

    public String getRoomName() {
        return roomName;
    }

    public void showPlayers() {

        System.out.println("Room: " + roomName);

        for (User u : players) {
            System.out.println(" - " + u.getUsername());
        }
    }
}
