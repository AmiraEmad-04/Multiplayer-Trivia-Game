package com.example.server;

import com.example.data.Question;
import com.example.objects.Game;

import java.io.*;
import java.util.*;

public class GameLoop implements Runnable {

    Game game;
    int questionTime;
    public volatile boolean gameFinished = false;

    public GameLoop(Game game, int questionTime) {
        this.game         = game;
        this.questionTime = questionTime;
    }

    public void run() {

        game.sendToAll("Game start");
        game.sendToAll("Questions: " + game.qs.size());
        game.sendToAll("Points: Easy=10 Medium=20 Hard=30");

        pause(2000);

        for (int q = 0; q < game.qs.size(); q++) {

            game.currentQuestionIndex = q;

            Question current = game.qs.get(q);

            game.resetAnswersForNewQuestion();

            game.sendToAll("");
            game.sendToAll("Question " + (q + 1) + " of " + game.qs.size());
            game.sendToAll("Category: " + current.category + "  Level: " + current.level);
            game.sendToAll(current.showQuestion());
            game.sendToAll("You have " + questionTime + " seconds.");

            game.answerWindowOpen = true;

            int timeLeft = questionTime;
            StringBuilder timeDisplay = new StringBuilder("TIMER:Remaining:");

            while (timeLeft > 0) {

                pause(1000);
                timeLeft--;

                if (timeLeft == 10 || timeLeft == 5 || timeLeft == 3 || timeLeft == 1) {
                    timeDisplay.append(" ").append(timeLeft);
                    game.sendToAll(timeDisplay.toString());
                }

                if (allPlayersAnswered()) {

                    game.sendToAll("All players answered.");
                    break;
                }
            }

            game.answerWindowOpen = false;

            // move cursor to new line after the timer overwrote it
            game.sendToAll("TIMER_END");
            game.sendToAll("Time is up.");

            evaluateAnswers(current, q);

            showCurrentScores();

            if (q < game.qs.size() - 1) {

                game.sendToAll("Next question soon...");
                pause(2000);
            }
        }

        showFinalResults();
        saveScores();
        gameFinished = true;
    }

    private void evaluateAnswers(Question current, int q) {

        game.sendToAll("Correct answer: " + current.correctAnswer);

        int points = game.getPoints(current.level);

        for (int i = 0; i < game.players; i++) {

            String theirAnswer = game.answers[i][q];

            if (theirAnswer.isEmpty()) {

                game.sendToAll(game.names[i] + " did not answer");

            } else if (theirAnswer.equals(current.correctAnswer)) {

                game.scores[i] += points;

                game.sendToAll(game.names[i] + " correct! +" + points + " points");

            } else {

                game.sendToAll(game.names[i] + " wrong (answered " + theirAnswer + ")");
            }
        }
    }

    private void showCurrentScores() {

        game.sendToAll("Scores");

        for (int i = 0; i < game.players; i++) {

            game.sendToAll(game.names[i] + ": " + game.scores[i] + " points");
        }
    }

    private void showFinalResults() {

        game.sendToAll("GAME OVER");
        game.sendToAll("Final scores:");

        for (int i = 0; i < game.players; i++) {

            game.sendToAll(game.names[i] + ": " + game.scores[i] + " points");
        }

        game.sendToAll("Answer details:");

        for (int i = 0; i < game.players; i++) {

            game.sendToAll("Player: " + game.names[i]);

            for (int q = 0; q < game.qs.size(); q++) {

                Question current = game.qs.get(q);
                String theirAnswer = game.answers[i][q];

                String result;
                if (theirAnswer.isEmpty()) {
                    result = "no answer";
                } else if (theirAnswer.equals(current.correctAnswer)) {
                    result = "correct";
                } else {
                    result = "wrong";
                }

                game.sendToAll("  Q" + (q + 1) + ": " + current.text);
                game.sendToAll("  Your answer: " + (theirAnswer.isEmpty() ? "none" : theirAnswer)
                        + " | Correct: " + current.correctAnswer
                        + " | " + result);
            }
        }
    }

    private boolean allPlayersAnswered() {

        for (int i = 0; i < game.players; i++) {

            if (!game.answered[i]) return false;
        }

        return true;
    }

    private void pause(int milliseconds) {

        try {

            Thread.sleep(milliseconds);

        } catch (Exception e) {
        }
    }

    private void saveScores() {

        String filePath = "src/main/java/com/example/data/scores.txt";

        LinkedHashMap<String, String> allScores = new LinkedHashMap<>();

        try {

            File file    = new File(filePath);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(":");

                if (parts.length == 2) {
                    allScores.put(parts[0], parts[1]);
                }
            }

            scanner.close();

        } catch (Exception e) {

            System.out.println("Could not read scores file");
        }

        for (int i = 0; i < game.players; i++) {

            String name     = game.names[i];
            int    newScore = game.scores[i];

            if (allScores.containsKey(name)) {

                allScores.put(name, allScores.get(name) + "," + newScore);

            } else {

                allScores.put(name, String.valueOf(newScore));
            }
        }

        try {

            FileWriter writer = new FileWriter(filePath);

            for (String name : allScores.keySet()) {
                writer.write(name + ":" + allScores.get(name) + "\n");
            }

            writer.close();

            System.out.println("Scores saved.");

        } catch (Exception e) {

            System.out.println("Save error");
        }
    }
}