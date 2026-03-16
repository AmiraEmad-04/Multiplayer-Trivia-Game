package com.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {

    public static void main(String[] args) {

        try {

            Socket socket = new Socket("127.0.0.1", 6660);

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream  dataInputStream  = new DataInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);

            String serverQuestion = dataInputStream.readUTF();
            System.out.println(serverQuestion);

            String clientName = scanner.nextLine();
            dataOutputStream.writeUTF(clientName);
            dataOutputStream.flush();


            boolean setupDone = false;

            while (!setupDone) {

                String serverMsg = dataInputStream.readUTF();


                if (serverMsg.equals("start")) {
                    setupDone = true;
                    break;
                }


                System.out.println(serverMsg);


                if (serverMsg.contains("1 - Single") || serverMsg.contains("Number of players")) {

                    String input = scanner.nextLine();
                    dataOutputStream.writeUTF(input);
                    dataOutputStream.flush();
                }
            }

            System.out.println("Game starting. Use A B C D to answer.");


            Thread readerThread = new Thread(() -> {

                try {

                    while (true) {

                        String msg = dataInputStream.readUTF();
                        System.out.println(msg);
                    }

                } catch (Exception e) {

                    System.out.println("Server closed");
                }
            });

            readerThread.start();


            while (true) {

                String input = scanner.nextLine();

                dataOutputStream.writeUTF(input);
                dataOutputStream.flush();


                if (input.equals("-")) {
                    break;
                }
            }

            scanner.close();
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();

        } catch (Exception e) {

            System.out.println("Connection error");
        }
    }
}