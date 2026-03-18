package com.example.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import com.example.objects.User;
import com.example.objects.Game;
import com.example.server.GameLoop;
import com.example.server.ServerSocketTCP;

public class GameOptionsService {

    private final DataInputStream  in;
    private final DataOutputStream out;
    private final User user;
    private static final TeamService teamService = new TeamService();
    private static final RoomService roomService = new RoomService();
    public GameLoop activeLoop = null;

    public GameOptionsService(DataInputStream in, DataOutputStream out, User user, int myIndex) {
        this.in   = in;
        this.out  = out;
        this.user = user;
    }

    public Game showMenu() throws Exception {
        while (true) {
            out.writeUTF("\n=>GAME MENU<=>\n" +
                    "1. Single Player\n" +
                    "2. Multiplayer (Teams)\n" +
                    "3. Public Game Room\n" +
                    "4. Random Trivia\n" +
                    "5. Quit\n" +
                    "\nChoose option:");
            out.flush();
            String choice = in.readUTF().trim();

            switch (choice) {
                case "1": {
                    Game g = handleSinglePlayer();
                    if (g != null) return g;
                    break;
                }
                case "2": {
                    Game g = teamService.teamMenu(user, in, out);
                    if (g != null) {
                        activeLoop = teamService.getLastLoop();
                        return g;
                    }
                    break;
                }
                case "3": {
                    Game g = roomService.run(user, in, out);
                    if (g != null) {
                        activeLoop = roomService.getLastLoop();
                        return g;
                    }
                    break;
                }
                case "4": {
                    Game g = handleRandomTrivia();
                    if (g != null) return g;
                    break;
                }
                case "5":
                case "-": {
                    out.writeUTF("Goodbye, see you again :)");
                    out.flush();
                    return null;
                }
                default: {
                    out.writeUTF("Invalid option");
                    out.flush();
                }
            }
        }
    }

    private Game handleSinglePlayer() throws Exception {
        out.writeUTF("Choose a category:" +
                "\n1. Geography" +
                "\n2. Math" +
                "\n3. Science" +
                "\n4. History" +
                "\n5. Computer" +
                "\n6. Mixed");
        out.flush();
        int catChoice;
        try {
            catChoice = Integer.parseInt(in.readUTF().trim());
        } catch (Exception e) {
            catChoice = 6;
        }
        String category;
        switch (catChoice) {
            case 1: category = "Geography"; break;
            case 2: category = "Math"; break;
            case 3: category = "Science"; break;
            case 4: category = "History"; break;
            case 5: category = "Computer"; break;
            default: category = "Mixed";
        }
        out.writeUTF("Choose difficulty:" +
                "\n1. Easy" +
                "\n2. Medium" +
                "\n3. Hard" +
                "\n4. Mixed");
        out.flush();
        int diffChoice;
        try { diffChoice = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) { diffChoice = 4; }
        String difficulty;
        switch (diffChoice) {
            case 1: difficulty = "Easy"; break;
            case 2: difficulty = "Medium"; break;
            case 3: difficulty = "Hard"; break;
            default: difficulty = "Mixed";
        }
        out.writeUTF("How many questions? (1-" + ServerSocketTCP.maxQuestions + ")");
        out.flush();
        int numQ;
        try {
            numQ = Integer.parseInt(in.readUTF().trim());
        } catch (Exception e) {
            numQ = ServerSocketTCP.maxQuestions;
        }
        if (numQ <= 0 || numQ > ServerSocketTCP.maxQuestions) numQ = ServerSocketTCP.maxQuestions;
        out.writeUTF("Starting | Category: " + category + " | Difficulty: " + difficulty + " | Questions: " + numQ);
        out.flush();
        Game game = new Game(ServerSocketTCP.pickQuestionsByFilter(category, difficulty, numQ), 1);
        game.addPlayer(user.getName(), out);
        activeLoop = new GameLoop(game, ServerSocketTCP.questionTime);
        new Thread(activeLoop).start();
        return game;
    }

    private Game handleRandomTrivia() throws Exception {
        out.writeUTF("How many questions do you want?");
        out.flush();
        int numberOfQuestions = ServerSocketTCP.maxQuestions;
        try { numberOfQuestions = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) {
            out.writeUTF("Invalid input. Using " + ServerSocketTCP.maxQuestions);
            out.flush();
        }
        Game game = RandomTriviaService.startGame(user.getName(), out, numberOfQuestions, ServerSocketTCP.questionTime);
        if (game != null) activeLoop = RandomTriviaService.getLastLoop();
        return game;
    }
}