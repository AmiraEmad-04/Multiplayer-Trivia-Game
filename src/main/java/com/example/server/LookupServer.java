package com.example.server;

import com.example.data.Question;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class LookupServer {

    private static final int PORT= 6661;
    private static final String QUESTIONS_FILE= "src/main/java/com/example/data/questions.txt";
    // questions load once on startup, shared across  threads
    private static final List<Question> allQuestions= new ArrayList<>();
    private static final List<String> rawLines= new ArrayList<>();

    public static void main(String[] args) {

        loadQuestions();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("[LookupServer] Started on port "+ PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[LookupServer] Game server connected");
                Thread handler = new Thread(new LookupHandler(clientSocket));
                handler.start();
            }

        } catch (Exception e) {
            System.out.println("[LookupServer] Fatal error: "+ e);
        }
    }

    private static void loadQuestions() {
        try (BufferedReader br = new BufferedReader(new FileReader(QUESTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\|");
                if (parts.length < 5)
                    continue;

                String text= parts[0].trim();
                String category= parts[1].trim();
                String level= parts[2].trim();
                String correctAnswer= parts[4].trim();

                ArrayList<String> choices = new ArrayList<>(
                        Arrays.asList(parts[3].trim().split(","))
                );
                while (choices.size() < 4) choices.add("N/A");
                allQuestions.add(new Question(text, category, level, choices, correctAnswer));
                rawLines.add(line);
            }

            System.out.println("[LookupServer] Loaded " + allQuestions.size() + " questions.");

        } catch (FileNotFoundException e) {
            System.out.println("[LookupServer] questions.txt not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static List<Integer> query(String category, String level, int count) {

        List<Integer> filtered = new ArrayList<>();

        for (int i = 0; i < allQuestions.size(); i++) {
            Question q = allQuestions.get(i);
            boolean catMatch= category.equalsIgnoreCase("null") || q.category.equalsIgnoreCase(category);
            boolean lvlMatch= level.equalsIgnoreCase("null") || q.level.equalsIgnoreCase(level);
            if (catMatch && lvlMatch)
                filtered.add(i);
        }

        Collections.shuffle(filtered);
        return filtered.subList(0, Math.min(count, filtered.size()));
    }
    static class LookupHandler implements Runnable {
        private final Socket socket;
        LookupHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try (
                    BufferedReader in= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out= new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true)
            ) {

                String request = in.readLine();
                if (request == null || request.trim().isEmpty()) {
                    out.println("ERROR: Empty request");
                    return;
                }
                System.out.println("[LookupServer] Request:" + request);

                String[] parts = request.trim().split("\\|");
                if (parts.length < 3) {
                    out.println("ERROR: Invalid format. Expected category|level|count");
                    return;
                }
                String category= parts[0].trim();
                String level= parts[1].trim();
                int count;
                try {
                    count = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    out.println("ERROR: count must be a number");
                    return;
                }

                List<Integer> indices = LookupServer.query(category, level, count);
                if (indices.isEmpty()) {
                    out.println("ERROR: No questions found for " + category+ "/" + level);
                    return;
                }
                for (int idx : indices) {
                    out.println(LookupServer.rawLines.get(idx));
                }

                out.println("END");
                System.out.println("[LookupServer] Sent " + indices.size()+ " questions.");

            } catch (Exception e) {
                System.out.println("[LookupServer] Handler error:" + e.getMessage());
            } finally {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
    }
}