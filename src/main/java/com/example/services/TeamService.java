package com.example.services;

import java.util.ArrayList;
import java.util.Scanner;
import com.example.objects.Team;
import com.example.objects.User;

public class TeamService {

    private final ArrayList<Team> teams;
    private final Scanner scanner;

    public TeamService() {
        teams = new ArrayList<>();
        scanner = new Scanner(System.in);
    }

    public void createTeam(User user) {
        System.out.print("Enter team name: ");
        String name = scanner.nextLine();
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(name)) {
                System.out.println("Team name already exists,try another name :(");
                return;
            }
        }
        Team team = new Team(name);
        team.addPlayer(user);
        teams.add(team);
        System.out.println(team.getName() + " is created successfully :)");
    }

    public void joinTeam(User user) {
        System.out.print("Enter team name you want to join: ");
        String name = scanner.nextLine();
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(name)) {
                t.addPlayer(user);
                System.out.println(user.getUsername() + " joined " + name);
                return;
            }
        }
        System.out.println("Team not found :(");
    }

    public void showTeams() {
        if (teams.isEmpty()) {
            System.out.println("No teams are created yet :(");
            return;
        }
        for (Team t : teams) {
            t.showAllPlayers();
        }
    }

    public void checkTeamsBalance() {
        if (teams.size() != 2) {
            System.out.println("Game requires 2 teams");
            return;
        }
        int team1 = teams.get(0).getSize();
        int team2 = teams.get(1).getSize();
        if (team1 == team2) {
            System.out.println("Teams are balanced,Game can start :)");
        } else {
            System.out.println("Teams are NOT balanced");
        }
    }

    public void teamMenu(User user) {
        boolean flag = true;
        while (flag) {
            System.out.println("\nChoose an option: ");
            System.out.println("1. Create Team");
            System.out.println("2. Join Team");
            System.out.println("3. Show Teams");
            System.out.println("4. Check teams Balance");
            System.out.println("5. Back");
            int option = scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:{
                    createTeam(user);
                    break;
                }
                case 2:{
                    joinTeam(user);
                    break;
                }
                case 3:{
                    showTeams();
                    break;
                }
                case 4:{
                    checkTeamsBalance();
                    break;
                }
                case 5:{
                    flag = false;
                    break;
                }
                default:{
                    System.out.println("Invalid option.");
                }
            }
        }
    }
}
