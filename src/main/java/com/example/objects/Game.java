package com.example.objects;

import com.example.data.Question;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class Game {

    public boolean answerWindowOpen;
    public int currentQuestionIndex;
    public String[] names;
    public int[] scores;
    DataOutputStream[] streams;

    public boolean[] answered;
    public boolean[] hasAnswered;

    public String[][] answers;
    public String[][] playerAnswers;

    public ArrayList<Question> qs;

    public int players = 0;
    public int numberOfPlayers = 0;

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

    public int getPoints(String level) {

        return switch (level) {
            case "Easy" -> 10;
            case "Medium" -> 20;
            case "Hard" -> 30;
            default -> 0;
        };

    }

    public void resetAnswersForNewQuestion() {

        for (int i = 0; i < players; i++) {

            answered[i] = false;
        }
    }
}