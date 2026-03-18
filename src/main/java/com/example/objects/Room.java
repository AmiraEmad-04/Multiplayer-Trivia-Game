package com.example.objects;

import java.io.DataOutputStream;
import java.util.ArrayList;

public class Room {
    private final String roomName;
    private final String category;
    private final String difficulty;
    private final int numQuestions;
    private final int maxPlayers;
    private final ArrayList<User> players;
    private boolean gameStarted;

    public Room(String roomName, String category, String difficulty, int numQuestions, int maxPlayers) {
        this.roomName = roomName;
        this.category = category;
        this.difficulty = difficulty;
        this.numQuestions = numQuestions;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.gameStarted = false;
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

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public ArrayList<User> getPlayers() {
        return players;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
}