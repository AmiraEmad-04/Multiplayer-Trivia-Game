package com.example.objects;

import java.util.ArrayList;

public class Team {

    private final String name;
    private final ArrayList<User> players;

    public Team(String name) {
        this.name = name;
        players = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addPlayer(User user) {
        players.add(user);
    }

    public int getSize(){
        return players.size();
    }

    public void showAllPlayers() {
        System.out.println("Team: " + name);
        System.out.println("Members: " + name);
        int i=1;
        for (User u : players) {
            System.out.println(i + ". " + u.getUsername());
            i++;
        }
    }
}
