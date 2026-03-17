package com.example.services;

import java.util.ArrayList;
import java.util.Collections;

public class RandomTriviaService {

    class Question {

        String text;
        String[] choices;
        char correct;

        Question(String text, String[] choices, char correct) {
            this.text = text;
            this.choices = choices;
            this.correct = correct;
        }
    }

    private ArrayList<Question> questions;

    public RandomTriviaService() {

        questions = new ArrayList<>();

        questions.add(new Question(
                "What is the capital of Canada?",
                new String[]{"A) Toronto", "B) Ottawa", "C) Montreal", "D) Vancouver"},
                'B'));

        questions.add(new Question(
                "Which planet has the most moons?",
                new String[]{"A) Earth", "B) Mars", "C) Jupiter", "D) Venus"},
                'C'));

        questions.add(new Question(
                "Who developed the theory of relativity?",
                new String[]{"A) Newton", "B) Einstein", "C) Tesla", "D) Galileo"},
                'B'));

    }

    public void startRandomGame() {

        Collections.shuffle(questions);

        System.out.println("Starting Random Trivia!");

        for (Question q : questions) {

            System.out.println("\n" + q.text);

            for (String c : q.choices) {
                System.out.println(c);
            }

            System.out.println("Answer will be checked by GameEngine later.");
        }

    }

}
