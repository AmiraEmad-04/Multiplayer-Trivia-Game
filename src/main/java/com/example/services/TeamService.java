package com.example.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import com.example.data.Question;
import com.example.objects.Team;
import com.example.objects.User;
import com.example.objects.Game;
import com.example.server.GameLoop;
import com.example.server.ServerSocketTCP;

public class TeamService {
    private final ArrayList<Team> teams;
    private Game activeGame = null;
    private GameLoop lastLoop = null;

    public TeamService() {
        teams = new ArrayList<>();
    }

    private boolean isUserInTeam(User user) {
        for (Team t : teams) {
            if (t.hasPlayer(user)) return true;
        }
        return false;
    }

    public void createTeam(User user, DataInputStream in, DataOutputStream out) throws Exception {
        if (isUserInTeam(user)) {
            out.writeUTF("You are already in a team");
            out.flush();
            return;
        }
        if (teams.size() == 2) {
            out.writeUTF("Only 2 teams are allowed");
            out.flush();
            return;
        }
        out.writeUTF("Enter team name:");
        out.flush();
        String name = in.readUTF();
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(name)) {
                out.writeUTF("Team name already exists");
                out.flush();
                return;
            }
        }
        Team team = new Team(name);
        user.setStream(out);
        team.addPlayer(user);
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
        out.writeUTF("Enter number of questions:");
        out.flush();
        int num = ServerSocketTCP.maxQuestions;
        try { num = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) {}
        team.setGameInfo(category, difficulty, num);
        teams.add(team);
        out.writeUTF("Team '" + name + "' created successfully.");
        out.flush();
    }

    public void joinTeam(User user, DataInputStream in, DataOutputStream out) throws Exception {
        if (isUserInTeam(user)) {
            out.writeUTF("You are already in a team!");
            out.flush();
            return;
        }
        out.writeUTF("Enter team name:");
        out.flush();
        String name = in.readUTF();
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(name)) {
                user.setStream(out);
                t.addPlayer(user);
                out.writeUTF(user.getUsername() + " joined team '" + name + "'");
                out.flush();
                return;
            }
        }
        out.writeUTF("Team not found.");
        out.flush();
    }

    public void showTeams(DataOutputStream out) throws Exception {
        if (teams.isEmpty()) {
            out.writeUTF("No teams created yet.");
            out.flush();
            return;
        }
        for (Team t : teams) {
            t.showAllPlayers(out);
        }
    }

    private synchronized Game startGame(DataOutputStream out) throws Exception {
        if (teams.size() != 2) {
            out.writeUTF("Need exactly 2 teams to start.");
            out.flush();
            return null;
        }
        int s1 = teams.get(0).getSize();
        int s2 = teams.get(1).getSize();
        if (s1 != s2 || s1 == 0) {
            out.writeUTF("Teams must be equal size and non-empty. Team 1: " + s1 + " | Team 2: " + s2);
            out.flush();
            return null;
        }
        if (activeGame != null) return activeGame;
        Team t = teams.get(0);
        ArrayList<Question> qs = ServerSocketTCP.pickQuestionsByFilter(t.getCategory(), t.getDifficulty(), t.getNumberOfQuestions());
        if (qs.isEmpty()) {
            out.writeUTF("No questions found for category: " + t.getCategory() + " difficulty: " + t.getDifficulty() + ". Try different settings.");
            out.flush();
            return null;
        }
        int totalPlayers = s1 + s2;
        activeGame = new Game(qs, totalPlayers);
        for (User u : teams.get(0).getPlayers()) activeGame.addPlayer(u.getUsername(), u.getStream());
        for (User u : teams.get(1).getPlayers()) activeGame.addPlayer(u.getUsername(), u.getStream());
        out.writeUTF("Teams are ready! Starting game...\nCategory: " + t.getCategory() + " | Difficulty: " + t.getDifficulty() + " | Questions: " + t.getNumberOfQuestions());
        out.flush();
        lastLoop = new GameLoop(activeGame, ServerSocketTCP.questionTime);
        new Thread(lastLoop).start();
        return activeGame;
    }

    public GameLoop getLastLoop() {
        return lastLoop;
    }

    public synchronized void reset() {
        teams.clear();
        activeGame = null;
        lastLoop = null;
    }

    public Game teamMenu(User user, DataInputStream in, DataOutputStream out) throws Exception {
        if (activeGame != null && lastLoop != null && lastLoop.gameFinished) {
            reset();
        }
        while (true) {
            if (activeGame != null) {
                out.writeUTF("Game is starting, Get ready");
                out.flush();
                return activeGame;
            }
            out.writeUTF("\n=>TEAM MENU<=\n" +
                    "1. Create Team\n" +
                    "2. Join Team\n" +
                    "3. Show Teams\n" +
                    "4. Start the Game\n" +
                    "5. Back\n" );
            out.flush();
            while (in.available() == 0) {
                if (activeGame != null) {
                    out.writeUTF("Game is starting! Get ready...");
                    out.flush();
                    return activeGame;
                }
                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {}
            }
            int option;
            try {
                option = Integer.parseInt(in.readUTF().trim());
            } catch (Exception e) {
                option = -1;
            }
            switch (option) {
                case 1: createTeam(user, in, out); break;
                case 2: joinTeam(user, in, out); break;
                case 3: showTeams(out); break;
                case 4: {
                    Game g = startGame(out);
                    if (g != null) return g;
                    break;
                }
                case 5: return null;
                default: out.writeUTF("Invalid option."); out.flush();
            }
        }
    }
}