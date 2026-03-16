package com.example.server;

import com.example.data.Question;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class Game {

    String[] names;
    int[] scores;
    DataOutputStream[] streams;

    boolean[] answered;
    boolean[] hasAnswered;

    String[][] answers;
    String[][] playerAnswers;

    ArrayList<Question> qs;

    boolean answerWindowOpen = false;

    int currentQuestionIndex = 0;

    int players = 0;
    int numberOfPlayers = 0;

    public Game(ArrayList<Question> qs, int maxPlayers) {

        this.qs = qs;

        names   = new String[maxPlayers];
        scores  = new int[maxPlayers];
        streams = new DataOutputStream[maxPlayers];

        answered = new boolean[maxPlayers];
        answers  = new String[maxPlayers][qs.size()];

        hasAnswered   = answered;
        playerAnswers = answers;

        for (int i = 0; i < maxPlayers; i++) {
            for (int j = 0; j < qs.size(); j++) {
                answers[i][j] = "";
            }
        }
    }

    public int addPlayer(String name, DataOutputStream stream) {

        int index = players;

        names[index]   = name;
        streams[index] = stream;

        players++;

        numberOfPlayers = players;

        return index;
    }

    public void sendToAll(String message) {

        for (int i = 0; i < players; i++) {

            try {

                streams[i].writeUTF(message);
                streams[i].flush();

            } catch (Exception e) {

                System.out.println(names[i] + " disconnected");
            }
        }
    }

    public void sendToOne(int playerIndex, String message) {

        try {

            streams[playerIndex].writeUTF(message);
            streams[playerIndex].flush();

        } catch (Exception e) {

            System.out.println(names[playerIndex] + " disconnected");
        }
    }

    public int getPoints(String level) {

        if (level.equals("Easy"))   return 10;
        if (level.equals("Medium")) return 20;
        if (level.equals("Hard"))   return 30;

        return 0;
    }

    public void resetAnswersForNewQuestion() {

        for (int i = 0; i < players; i++) {

            answered[i] = false;
        }
    }
}