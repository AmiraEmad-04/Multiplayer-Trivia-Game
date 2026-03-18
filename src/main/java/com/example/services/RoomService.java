package com.example.services;

import com.example.data.Question;
import com.example.objects.Room;
import com.example.objects.User;
import com.example.objects.Game;
import com.example.server.GameLoop;
import com.example.server.ServerSocketTCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RoomService {

    private GameLoop lastLoop = null;
    private Game lastGame = null;
    private final List<Room> rooms;
    private final int minPlayers;
    private final int maxPlayers;

    public RoomService() {
        rooms = new ArrayList<>();
        minPlayers = 2;
        maxPlayers = 4;
    }

    public Game createRoom(User user, DataInputStream in, DataOutputStream out) throws Exception {
        out.writeUTF("Enter room name:");
        out.flush();
        String name = in.readUTF();
        for (Room r : rooms) {
            if (r.getRoomName().equalsIgnoreCase(name)) {
                out.writeUTF("Room name already exists. Please choose a different name.");
                out.flush();
                return null;
            }
        }
        out.writeUTF("Choose a category:" +
                "\n1. Geography" +
                "\n2. Math" +
                "\n3. Science" +
                "\n4. History" +
                "\n5. Computer" +
                "\n6. Mixed");
        out.flush();
        int categoryChoice;
        try { categoryChoice = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) { categoryChoice = 6; }
        String category;
        switch (categoryChoice) {
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
        int difficultyChoice;
        try { difficultyChoice = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) { difficultyChoice = 4; }
        String difficulty;
        switch (difficultyChoice) {
            case 1: difficulty = "Easy"; break;
            case 2: difficulty = "Medium"; break;
            case 3: difficulty = "Hard"; break;
            default: difficulty = "Mixed";
        }
        out.writeUTF("Enter number of questions:");
        out.flush();
        int numQuestions = ServerSocketTCP.maxQuestions;
        try {
            numQuestions = Integer.parseInt(in.readUTF().trim());
        } catch (Exception e) {}
        if (numQuestions <= 0) numQuestions = ServerSocketTCP.maxQuestions;
        int playersForRoom = minPlayers;
        while (true) {
            out.writeUTF("Enter number of players (" + minPlayers + " - " + maxPlayers + "):");
            out.flush();
            try { playersForRoom = Integer.parseInt(in.readUTF().trim()); } catch (Exception e) {}
            if (playersForRoom < minPlayers || playersForRoom > maxPlayers) {
                out.writeUTF("Must be between " + minPlayers + " and " + maxPlayers);
                out.flush();
            } else break;
        }
        user.setStream(out);
        Room room = new Room(name, category, difficulty, numQuestions, playersForRoom);
        room.addPlayer(user);
        rooms.add(room);
        out.writeUTF("Room '" + name + "' created!\nCategory: " + category + " | Difficulty: " + difficulty + " | Questions: " + numQuestions + " | Max Players: " + playersForRoom);
        out.writeUTF("Waiting for players (" + room.size() + "/" + playersForRoom + ")");
        out.flush();
        return waitForRoom(room, out);
    }

    public Game joinRandomRoom(User user, DataInputStream in, DataOutputStream out) throws Exception {
        Room availableRoom = null;
        for (Room r : rooms) {
            if (!r.isFull() && !r.isGameStarted()) {
                availableRoom = r;
                break;
            }
        }
        if (availableRoom == null) {
            out.writeUTF("No available rooms. Creating a new room...");
            out.flush();
            return createRoom(user, in, out);
        }
        user.setStream(out);
        availableRoom.addPlayer(user);
        out.writeUTF("Joined room '" + availableRoom.getRoomName() + "' (" + availableRoom.size() + "/" + availableRoom.getMaxPlayers() + ")");
        out.flush();
        return waitForRoom(availableRoom, out);
    }

    private Game waitForRoom(Room room, DataOutputStream out) {
        while (!room.isFull()) {
            try {
                try {
                    out.writeUTF("Waiting for players (" + room.size() + "/" + room.getMaxPlayers() + ")");
                    out.flush();
                } catch (Exception e) {}
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        synchronized (room) {
            if (!room.isGameStarted()) {
                room.setGameStarted(true);
                lastGame = startGame(room, out);
                return lastGame;
            }
        }
        while (lastGame == null) {
            try { Thread.sleep(100); } catch (Exception ignored) {}
        }
        try {
            out.writeUTF("Game is starting! Get ready...");
            out.flush();
        } catch (Exception ignored) {}
        return lastGame;
    }

    private Game startGame(Room room, DataOutputStream out) {
        try {
            out.writeUTF("Room '" + room.getRoomName() + "' is full! Starting game...");
            out.flush();
        } catch (Exception e) {}
        ArrayList<Question> qs = ServerSocketTCP.pickQuestionsByFilter(room.getCategory(), room.getDifficulty(), room.getNumQuestions());
        Game game = new Game(qs, room.getMaxPlayers());
        for (User u : room.getPlayers()) game.addPlayer(u.getUsername(), u.getStream());
        lastLoop = new GameLoop(game, ServerSocketTCP.questionTime);
        new Thread(lastLoop).start();
        return game;
    }

    public GameLoop getLastLoop() { return lastLoop; }
    public Game run(User user, DataInputStream in, DataOutputStream out) throws Exception {
        out.writeUTF("\n=>PUBLIC ROOM<=" +
                "\n1. Create a room" +
                "\n2. Join a room" +
                "\n3. Back\n");
        out.flush();
        int choice;
        try {
            choice = Integer.parseInt(in.readUTF().trim());
        } catch (Exception e) { choice = 3; }
        switch (choice) {
            case 1: return createRoom(user, in, out);
            case 2: return joinRandomRoom(user, in, out);
            case 3: return null;
            default:
                out.writeUTF("Invalid option");
                out.flush();
                return null;
        }
    }
}