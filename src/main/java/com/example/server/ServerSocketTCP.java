package com.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.ServerSocket;

public class ServerSocketTCP extends GameService implements Runnable {

    private final Socket socket;

    public ServerSocketTCP(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6660);
            System.out.println("Server started, waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                ServerSocketTCP serverSocketTCP = new ServerSocketTCP(socket);
                Thread thread = new Thread(serverSocketTCP);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String clientName = dataInputStream.readUTF();
            System.out.println(clientName + " joined");
            String str = dataInputStream.readUTF();
            while (!str.equals("0")) {
//                if (str.equals("1")) {
//                    String receiverVal = dataInputStream.readUTF();
//                    String messageVal = dataInputStream.readUTF();
//                    String result = saveMessage(clientName, receiverVal, messageVal);
//                    dataOutputStream.writeUTF(result);
//                    dataOutputStream.flush();
//
//                } else if (str.equals("2")) {
//                    List<String> msgs = readMessages(clientName);
//                    if (msgs.isEmpty()) {
//                        dataOutputStream.writeUTF("No messages found");
//                        dataOutputStream.flush();
//                    } else {
//                        for (String msg : msgs) {
//                            dataOutputStream.writeUTF(msg);
//                            dataOutputStream.flush();
//                        }
//                        dataOutputStream.writeUTF("DONE");
//                        dataOutputStream.flush();
//                    }
//                }
                str = dataInputStream.readUTF();
            }
            System.out.println(clientName + " disconnected");
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}