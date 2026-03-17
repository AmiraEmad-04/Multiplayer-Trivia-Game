package com.example.data;

import java.util.ArrayList;

public class Question {

    public String text;
    public String category;
    public String level;
    ArrayList<String> choices;
    public String correctAnswer;

    public Question(String text, String category, String level,
                    ArrayList<String> choices, String correctAnswer) {

        this.text = text;
        this.category = category;
        this.level = level;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
    }

    public String showQuestion() {
        String result = text + "\n";
        result += "A. " + choices.get(0) + "\n";
        result += "B. " + choices.get(1) + "\n";
        result += "C. " + choices.get(2) + "\n";
        result += "D. " + choices.get(3);
        return result;
    }
}