package com.example.services;

import com.example.data.Question;
import com.example.objects.User;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class AuthService {

    //load from users.txt
    private static final Map<String, User> users = new HashMap<>();
    private static final String USERS_FILE = "src/main/java/com/example/data/users.txt";
    private static final String SCORES_FILE = "src/main/java/com/example/data/scores.txt";
    private static final String CONFIG_FILE = "src/main/java/com/example/data/config.txt";
    //for LookupServer connection
    private static final String LOOKUP_HOST = "127.0.0.1";
    private static final int LOOKUP_PORT = 6661;
    // Config info
    private static int minPlayers = 2;
    private static int maxPlayers= 4;
    private static int questionTime= 15;
    private static int maxQuestions= 5;
    private static int publicRoomMinPlayers= 2;
    private static int publicRoomMaxPlayers= 4;

    //  call once ServerSocketTCP
    public static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(",");
                if (parts.length < 2)
                    continue;

                String username= parts[0].trim();
                String password= parts[1].trim();
                users.put(username, new User(username, password));
            }

            System.out.println("[AuthService] Loaded " + users.size() + " users.");

        } catch (FileNotFoundException e) {
            System.out.println("[AuthService] users.txt not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load config.txt → format: key=value
    public static void loadConfig() {

        try (BufferedReader br= new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] parts = line.split("=");
                if (parts.length < 2)
                    continue;

                String key= parts[0].trim();
                String value= parts[1].trim();
                switch (key) {
                    case "minPlayers":
                        minPlayers= Integer.parseInt(value);
                        break;
                    case "maxPlayers":
                        maxPlayers= Integer.parseInt(value);
                        break;
                    case "questionTime":
                        questionTime= Integer.parseInt(value);
                        break;
                    case "maxQuestions":
                        maxQuestions= Integer.parseInt(value);
                        break;
                    case "publicRoomMinPlayers":
                        publicRoomMinPlayers= Integer.parseInt(value);
                        break;
                    case "publicRoomMaxPlayers":
                        publicRoomMaxPlayers= Integer.parseInt(value);
                        break;
                    default:
                        System.out.println("[AuthService] Unknown config key: "+ key);
                }
            }

            System.out.println("[AuthService] Config loaded."
                    + " minPlayers="+ minPlayers
                    + " maxPlayers="+ maxPlayers
                    + " questionTime="+ questionTime + "s"
                    + " maxQuestions="+ maxQuestions);

        } catch (FileNotFoundException e) {
            System.out.println("[AuthService] config.txt not found – using defaults.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //  "200" | "401" | "404"
    public static String login(String username, String password) {

        if (!users.containsKey(username))
            return "404";
        if (!users.get(username).getPassword().equals(password))
            return "401";
        return "200";
    }
    // Register
    public static String register(String name, String username, String password) {

        if (users.containsKey(username))
            return "USERNAME_TAKEN";
        users.put(username, new User(name, username, password));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            bw.write(username + "," + password);
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "REGISTERED";
    }
    // lockup client
    public static ArrayList<Question> fetchQuestionsFromLookup(String category, String level, int count) {
        ArrayList<Question> questions = new ArrayList<>();
        try (
                Socket socket= new Socket(LOOKUP_HOST, LOOKUP_PORT);
                PrintWriter out= new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in= new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            // Send request level or count
            out.println(category + "|" + level + "|" + count);
            // read response line by line til END
            String line;
            while ((line = in.readLine())!= null) {
                if (line.equals("END"))
                    break;
                if (line.startsWith("ERROR:")) {
                    System.out.println("[AuthService] LookupServer error:"+ line);
                    break;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 5)
                    continue;

                String text= parts[0].trim();
                String cat= parts[1].trim();
                String lvl= parts[2].trim();
                String correctAnswer= parts[4].trim();
                ArrayList<String> choices = new ArrayList<>(
                        Arrays.asList(parts[3].trim().split(","))
                );

                while (choices.size() < 4) choices.add("N/A");
                questions.add(new Question(text, cat, lvl, choices, correctAnswer));
            }

            System.out.println("[AuthService] Fetched " + questions.size() + " questions from LookupServer.");

        } catch (Exception e) {
            System.out.println("[AuthService] Could not connect to LookupServer: " + e.getMessage());
        }

        return questions;
    }

    //admin panel
    public static String getPlayerWithMostWins() {
        String topName = "None";
        int topWins = -1;

        try (BufferedReader br = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(":");
                if (parts.length < 2)
                    continue;

                String username = parts[0].trim();
                if (username.equalsIgnoreCase("null"))
                    continue;

                // count number of wins = number of score entries
                int wins = parts[1].split(",").length;
                if (wins > topWins) {
                    topWins = wins;
                    topName  = username;
                }
            }

        } catch (Exception e) {
            System.out.println("[AuthService] Could not read scores.txt");
        }

        return topName + " (" + topWins +" wins)";
    }
    public static int getTotalQuestionsPlayed() {

        int totalGames = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(":");
                if (parts.length < 2)
                    continue;
                if (parts[0].trim().equalsIgnoreCase("null"))
                    continue;
                totalGames += parts[1].split(",").length;
            }

        } catch (Exception e) {
            System.out.println("[AuthService] Could not read scores.txt");
        }

        return totalGames * maxQuestions;
    }

    public static int getHighestScore() {

        int highest = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(SCORES_FILE))) {

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(":");
                if (parts.length < 2)
                    continue;
                if (parts[0].trim().equalsIgnoreCase("null"))
                    continue;

                for (String s : parts[1].split(",")) {
                    try {
                        int v = Integer.parseInt(s.trim());
                        if (v > highest) highest = v;
                    } catch (NumberFormatException ignored) {}
                }
            }

        } catch (Exception e) {
            System.out.println("[AuthService] Could not read scores.txt");
        }

        return highest;
    }
    public static User getUser(String username) {
        return users.get(username);
    }
    public static boolean isAdmin(String username) {
        return "admin".equalsIgnoreCase(username);
    }
}