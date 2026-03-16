package com.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;

import com.example.data.Question;

public class ServerSocketTCP extends GameService implements Runnable {

    private final Socket socket;
    private Game game;
    private int myIndex;
    private String clientName;

    static ArrayList<Question> allQuestions = new ArrayList<>();
    static Game currentGame = null;

    static int minPlayers    = 2;
    static int maxPlayers    = 4;
    static int questionTime  = 15;
    static int maxQuestions  = 5;

    static boolean gameStarted = false;

    public ServerSocketTCP(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {

        loadConfig();
        loadQuestions();

        ArrayList<Question> gameQuestions = pickRandomQuestions(maxQuestions);
        currentGame = new Game(gameQuestions, maxPlayers);

        try {

            ServerSocket serverSocket = new ServerSocket(6660);

            System.out.println("Server started");
            System.out.println("Players needed: " + minPlayers + " to " + maxPlayers);

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println("Client connected");

                ServerSocketTCP serverSocketTCP = new ServerSocketTCP(socket);

                Thread thread = new Thread(serverSocketTCP);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {

        try {

            DataInputStream dataInputStream   = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dataOutputStream.writeUTF("Enter your name");

            // FIX: assign directly to clientName (the class field)
            // not a new local variable, so this.clientName is set correctly
            clientName = dataInputStream.readUTF();
            System.out.println(clientName + " joined");

            // FIX: now clientName is set before addPlayer, so name is saved correctly
            myIndex = currentGame.addPlayer(clientName, dataOutputStream);

            // ---- first player is the host: they choose the game mode ----
            if (myIndex == 0) {

                dataOutputStream.writeUTF("Welcome " + clientName + "\n1 - Single\n2 - Multiplayer");
                dataOutputStream.flush();

                String choice = dataInputStream.readUTF().trim();

                if (choice.equals("1")) {

                    // single player: set minPlayers to 1 so game starts alone
                    minPlayers = 1;
                    dataOutputStream.writeUTF("Single player mode");
                    dataOutputStream.flush();

                } else {

                    // multiplayer: ask how many players
                    dataOutputStream.writeUTF("Number of players (2-" + maxPlayers + ")");
                    dataOutputStream.flush();

                    String numInput = dataInputStream.readUTF().trim();

                    try {

                        int chosenPlayers = Integer.parseInt(numInput);

                        if (chosenPlayers < 2 || chosenPlayers > maxPlayers) {

                            dataOutputStream.writeUTF("Invalid number. Using " + minPlayers);
                            dataOutputStream.flush();

                        } else {

                            minPlayers = chosenPlayers;
                            dataOutputStream.writeUTF("Waiting for " + minPlayers + " players");
                            dataOutputStream.flush();
                        }

                    } catch (Exception e) {

                        dataOutputStream.writeUTF("Invalid input. Using " + minPlayers);
                        dataOutputStream.flush();
                    }
                }

            } else {

                // FIX: merged into one message so client only reads once
                dataOutputStream.writeUTF("Welcome " + clientName + ". Waiting for other players...");
                dataOutputStream.flush();
            }

            // FIX: added small wait for non-host players
            // this gives the host time to finish choosing before we check numberOfPlayers
            if (myIndex != 0) {
                try { Thread.sleep(500); } catch (Exception e) {}
            }

            // ---- check if enough players joined to start ----
            if (currentGame.numberOfPlayers == minPlayers && !gameStarted) {

                gameStarted = true;

                currentGame.sendToAll("All players joined");

                 currentGame.sendToAll("start");

                GameLoop loop = new GameLoop(currentGame, questionTime);
                Thread gameThread = new Thread(loop);
                gameThread.start();
            }

             while (true) {

                String message = dataInputStream.readUTF().trim();

                if (message.equals("-")) {

                    dataOutputStream.writeUTF("You left the game");
                    break;
                }

                 String answer = message.toUpperCase();

                if (!answer.equals("A") && !answer.equals("B")
                        && !answer.equals("C") && !answer.equals("D")) {

                    dataOutputStream.writeUTF("Enter A B C or D");
                    continue;
                }

                if (!currentGame.answerWindowOpen) {

                    dataOutputStream.writeUTF("Wait for the question");
                    continue;
                }

                if (currentGame.hasAnswered[myIndex]) {

                    dataOutputStream.writeUTF("You already answered");
                    continue;
                }


                currentGame.playerAnswers[myIndex][currentGame.currentQuestionIndex] = answer;

                currentGame.hasAnswered[myIndex] = true;

                dataOutputStream.writeUTF("Answer received");
            }

            System.out.println(clientName + " disconnected");

            dataInputStream.close();
            dataOutputStream.close();
            socket.close();

        } catch (Exception e) {

            System.out.println(clientName + " disconnected");
        }
    }

    static void loadConfig() {

        try {

            File file = new File("src/main/java/com/example/data/config.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length != 2) continue;

                String key   = parts[0].trim();
                String value = parts[1].trim();

                if (key.equals("minPlayers"))   minPlayers   = Integer.parseInt(value);
                if (key.equals("maxPlayers"))   maxPlayers   = Integer.parseInt(value);
                if (key.equals("questionTime")) questionTime = Integer.parseInt(value);
                if (key.equals("maxQuestions")) maxQuestions = Integer.parseInt(value);
            }

            scanner.close();

            System.out.println("Config loaded");

        } catch (Exception e) {

            System.out.println("Config file not found");
        }
    }

    static void loadQuestions() {

        try {

            File file = new File("src/main/java/com/example/data/questions.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length != 5) continue;

                String text          = parts[0].trim();
                String category      = parts[1].trim();
                String level         = parts[2].trim();
                String choicesString = parts[3].trim();
                String correctAnswer = parts[4].trim();

                String[] choiceArray = choicesString.split(",");

                ArrayList<String> choices = new ArrayList<>();

                for (String choice : choiceArray) {
                    choices.add(choice.trim());
                }

                Question q = new Question(text, category, level, choices, correctAnswer);

                allQuestions.add(q);
            }

            scanner.close();

            System.out.println("Questions loaded: " + allQuestions.size());

        } catch (Exception e) {

            System.out.println("Error loading questions");
        }
    }

    static ArrayList<Question> pickRandomQuestions(int count) {

        ArrayList<Question> copy = new ArrayList<>(allQuestions);

        Collections.shuffle(copy);

        ArrayList<Question> picked = new ArrayList<>();

        for (int i = 0; i < count && i < copy.size(); i++) {

            picked.add(copy.get(i));
        }

        return picked;
    }
}