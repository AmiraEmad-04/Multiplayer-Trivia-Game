package com.example.services;

import java.util.Scanner;

import com.example.objects.User;

public class GameOptionsService {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        TeamService service = new TeamService();
        System.out.print("Enter username (dummy login): ");
        String username = scanner.nextLine();
        User currentUser = new User(username);
        boolean flag = true;

        while (flag) {
            System.out.println("\n-----GAME MENU-----");
            System.out.println("1. Single Player");
            System.out.println("2. Multiplayer (Teams)");
            System.out.println("3. Public Game Room");
            System.out.println("4. Random Trivia");
            System.out.println("5. Quit");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:{
                    System.out.println("Starting single player game->");
                    break;
                }
                case 2:{
                    System.out.println("Starting Multiplayer game->");
                    service.teamMenu(currentUser);
                    break;
                }
                case 3:{
                    System.out.println("Joining public room->");
                    break;
                }
                case 4:{
                    System.out.println("Starting random trivia->");
                    break;
                }
                case 5:{
                    flag = false;
                    System.out.println("Goodbye,see you again :)");
                    break;
                }
                default:{
                    System.out.println("Invalid option");
                }
            }
        }
    }
}
