package com.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 6660);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            String msg = "";
            do {
                System.out.println("Enter your name: ");
                String myName = scanner.nextLine();
                dataOutputStream.writeUTF(myName);
                dataOutputStream.flush();
                System.out.println("Quit(0)?");
                msg = scanner.nextLine();
                dataOutputStream.writeUTF(msg);
                dataOutputStream.flush();
//
//                if (msg.equals("1")) {
//                    System.out.println("Enter to whom you want to send: ");
//                    String receiverName = scanner.nextLine();
//                    dataOutputStream.writeUTF(receiverName);
//                    dataOutputStream.flush();
//
//                    System.out.println("Enter your message: ");
//                    String message = scanner.nextLine();
//                    dataOutputStream.writeUTF(message);
//                    dataOutputStream.flush();
//
//                    String response = dataInputStream.readUTF();
//                    if (response.equals("MAX")) {
//                        System.out.println("Inbox of " + receiverName + " is full, message not saved");
//                    } else {
//                        System.out.println("Message sent successfully");
//                    }
//
//                } else if (msg.equals("2")) {
//                    String response = dataInputStream.readUTF();
//                    while (!response.equals("No messages found") && !response.equals("DONE")) {
//                        System.out.println("You got a message: " + response);
//                        response = dataInputStream.readUTF();
//                    }
//                    if (response.equals("No messages found")) {
//                        System.out.println("No messages found");
//                    }
//                }

            } while (!msg.equals("0"));
            scanner.close();
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}