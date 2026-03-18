package com.example.objects;

import java.io.DataOutputStream;
import java.util.ArrayList;

public class Team {
    private final String name;
    private final ArrayList<User> players;
    private String category;
    private String difficulty;
    private int numberOfQuestions;

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addPlayer(User user) {
        players.add(user);
    }

    public boolean hasPlayer(User user) {
        return players.contains(user);
    }

    public int getSize() {
        return players.size();
    }

    public ArrayList<User> getPlayers() {
        return players;
    }

    public void showAllPlayers(DataOutputStream out) throws Exception {
        out.writeUTF("\nTeam: " + name);
        for (User u : players) {
            out.writeUTF("- " + u.getUsername());
        }
        out.flush();
    }

    public void setGameInfo(String category, String difficulty, int numberOfQuestions) {
        this.category = category;
        this.difficulty = difficulty;
        this.numberOfQuestions = numberOfQuestions;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }
}