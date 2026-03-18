package com.example.services;

import com.example.data.Question;
import com.example.objects.Game;
import com.example.server.GameLoop;
import com.example.server.ServerSocketTCP;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class RandomTriviaService {

    private static GameLoop lastLoop = null;

    public static Game startGame(String playerName, DataOutputStream stream, int numberOfQuestions, int questionTime) {
        ArrayList<Question> copy = new ArrayList<>(ServerSocketTCP.allQuestions);
        Collections.shuffle(copy);
        ArrayList<Question> picked = new ArrayList<>();
        for (int i = 0; i < numberOfQuestions && i < copy.size(); i++){
            picked.add(copy.get(i));
        }
        if (picked.isEmpty()) {
            try {
                stream.writeUTF("No questions available.");
                stream.flush();
            } catch (Exception e) {}
            return null;
        }
        Game game = new Game(picked, 1);
        game.addPlayer(playerName, stream);
        lastLoop = new GameLoop(game, questionTime);
        new Thread(lastLoop).start();
        return game;
    }

    public static GameLoop getLastLoop() {
        return lastLoop;
    }
}