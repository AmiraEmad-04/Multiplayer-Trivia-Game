package com.example.server;

import com.example.data.Question;
import com.example.objects.Game;
import com.example.objects.User;
import com.example.services.AuthService;
import com.example.services.GameOptionsService;
import com.example.services.GameService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSocketTCP extends GameService implements Runnable {

    private final Socket socket;
    private String clientName;

    public static ArrayList<Question> allQuestions = new ArrayList<>();
    public static Game currentGame = null;
    public static int questionTime = 15;
    public static int maxQuestions = 5;

    private static final AtomicInteger connectedPlayers = new AtomicInteger(0);

    public ServerSocketTCP(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        AuthService.loadUsers();
        AuthService.loadConfig();
        // fetch all questions from LookupServer (null = no filter = get everything)
        allQuestions = AuthService.fetchQuestionsFromLookup("null", "null", 9999);
        System.out.println("Questions fetched from LookupServer: " + allQuestions.size());
        currentGame = null;

        try {
            ServerSocket serverSocket = new ServerSocket(6660);
            System.out.println("Server Started");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(new ServerSocketTCP(socket)).start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        DataInputStream  in  = null;
        DataOutputStream out = null;

        try {
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            connectedPlayers.incrementAndGet();

            User loggedInUser = handleAuthentication(in, out);

            if (loggedInUser == null) {
                out.writeUTF("Goodbye!");
                out.flush();
                connectedPlayers.decrementAndGet();
                return;
            }

            clientName = loggedInUser.getUsername();
            loggedInUser.setStream(out);
            System.out.println(clientName + " joined");

            if (AuthService.isAdmin(clientName)) {
                handleAdminSession(in, out);
            } else {
                runGameSession(in, out, loggedInUser);
            }

            System.out.println(clientName + " disconnected");
            connectedPlayers.decrementAndGet();

        } catch (Exception e) {
            System.out.println((clientName != null ? clientName : "Client") + " disconnected: " + e.getMessage());
            connectedPlayers.decrementAndGet();
        } finally {
            try { if (in  != null) in.close();  } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void runGameSession(DataInputStream in, DataOutputStream out, User loggedInUser) throws Exception {
        GameOptionsService options = new GameOptionsService(in, out, loggedInUser, 0);

        Game activeGame;
        while ((activeGame = options.showMenu()) != null) {

            GameLoop activeLoop = options.activeLoop;

            // find this player's index in the game
            int myIndex = -1;
            for (int i = 0; i < activeGame.players; i++) {
                if (clientName.equals(activeGame.names[i])) {
                    myIndex = i;
                    break;
                }
            }

            // read answers until game finishes or player quits
            while (myIndex >= 0) {

                // game finished — break back to menu
                if (activeLoop != null && activeLoop.gameFinished) {
                    out.writeUTF("--- Game over! Returning to menu... ---");
                    out.flush();
                    break;
                }

                // poll for input without blocking
                if (in.available() == 0) {
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                    continue;
                }

                String message = in.readUTF().trim();

                if (message.equals("-")) {
                    out.writeUTF("You left the game");
                    break;
                }

                String answer = message.toUpperCase();

                if (!answer.equals("A") && !answer.equals("B")
                        && !answer.equals("C") && !answer.equals("D")) {
                    out.writeUTF("Enter A B C or D");
                    continue;
                }

                if (!activeGame.answerWindowOpen) {
                    out.writeUTF("Wait for the question");
                    continue;
                }

                if (activeGame.hasAnswered[myIndex]) {
                    out.writeUTF("You already answered");
                    continue;
                }

                activeGame.playerAnswers[myIndex][activeGame.currentQuestionIndex] = answer;
                activeGame.hasAnswered[myIndex] = true;
                out.writeUTF("Answer received");
            }
        }
    }

    private User handleAuthentication(DataInputStream in, DataOutputStream out) throws Exception {
        while (true) {
            out.writeUTF("Welcome\n1 - Login\n2 - Register\n- - Quit");
            out.flush();

            String choice = in.readUTF().trim();

            if (choice.equals("-")) return null;

            if (choice.equals("1")) {
                out.writeUTF("Enter username:");
                out.flush();
                String username = in.readUTF().trim();

                out.writeUTF("Enter password:");
                out.flush();
                String password = in.readUTF().trim();

                String result = AuthService.login(username, password);

                if (result.equals("200")) {
                    out.writeUTF("Login successful! Welcome, " + AuthService.getUser(username).getName() + "!");
                    out.flush();
                    return AuthService.getUser(username);
                } else if (result.equals("401")) {
                    out.writeUTF("ERROR 401: Wrong password. Try again.");
                    out.flush();
                } else if (result.equals("404")) {
                    out.writeUTF("ERROR 404: Username not found. Try again.");
                    out.flush();
                }

            } else if (choice.equals("2")) {
                out.writeUTF("Enter your name:");
                out.flush();
                String name = in.readUTF().trim();

                out.writeUTF("Choose a username:");
                out.flush();
                String username = in.readUTF().trim();

                out.writeUTF("Choose a password:");
                out.flush();
                String password = in.readUTF().trim();

                String result = AuthService.register(name, username, password);

                if (result.equals("REGISTERED")) {
                    out.writeUTF("Registered successfully! Please login.");
                    out.flush();
                } else {
                    out.writeUTF("ERROR: Username '" + username + "' is already taken. Try another.");
                    out.flush();
                }

            } else {
                out.writeUTF("Invalid option. Enter 1, 2, or -");
                out.flush();
            }
        }
    }

    private void handleAdminSession(DataInputStream in, DataOutputStream out) throws Exception {
        while (true) {
            out.writeUTF("=== Admin Panel ===\n" +
                    "1- Total players connected\n" +
                    "2- Player with most wins\n" +
                    "3- Total questions played\n" +
                    "4- Highest score ever recorded\n" +
                    "-- Logout");
            out.flush();

            String choice = in.readUTF().trim();

            if (choice.equals("-")) {
                out.writeUTF("Admin logged out.");
                out.flush();
                return;
            } else if (choice.equals("1")) {
                out.writeUTF("Total connected players: " + connectedPlayers.get());
                out.flush();
            } else if (choice.equals("2")) {
                out.writeUTF("Player with most wins: " + AuthService.getPlayerWithMostWins());
                out.flush();
            } else if (choice.equals("3")) {
                out.writeUTF("Total questions played: " + AuthService.getTotalQuestionsPlayed());
                out.flush();
            } else if (choice.equals("4")) {
                out.writeUTF("Highest score ever: " + AuthService.getHighestScore());
                out.flush();
            } else {
                out.writeUTF("Invalid option.");
                out.flush();
            }
        }
    }

    public static ArrayList<Question> pickQuestionsByFilter(String category, String difficulty, int count) {
        // send request to LookupServer with the filter
        String cat = category.equalsIgnoreCase("Mixed") ? "null" : category;
        String diff = difficulty.equalsIgnoreCase("Mixed") ? "null" : difficulty;
        ArrayList<Question> result = AuthService.fetchQuestionsFromLookup(cat, diff, count);
        if (result.isEmpty()) {
            System.out.println("LookupServer returned no questions for " + category + "/" + difficulty);
        }
        return result;
    }
}