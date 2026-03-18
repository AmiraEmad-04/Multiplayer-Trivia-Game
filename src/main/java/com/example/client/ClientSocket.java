package com.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("127.0.0.1", 6660);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream  in  = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            boolean authenticated = false;
            while (!authenticated) {
                String msg = in.readUTF();
                System.out.println(msg);

                if (msg.startsWith("Goodbye")) { socket.close(); return; }
                if (msg.startsWith("Login successful")) { authenticated = true; break; }

                if (msg.startsWith("ERROR") ||
                        msg.startsWith("Registered successfully") ||
                        msg.startsWith("Invalid option")) {
                    continue;
                }

                String input = scanner.nextLine().trim();
                out.writeUTF(input);
                out.flush();
                if (input.equals("-")) { socket.close(); return; }
            }

            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        // TIMER_END: move to new line so answer input is not overwritten
                        if (msg.equals("TIMER_END")) {
                            System.out.println();
                            continue;
                        }
                        // TIMER messages overwrite the same line
                        if (msg.startsWith("TIMER:")) {
                            System.out.print("\r" + msg.substring(6) + "   ");
                        } else {
                            System.out.println(msg);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Server closed.");
                }
            });
            reader.setDaemon(true);
            reader.start();

            while (true) {
                String input = scanner.nextLine().trim();
                out.writeUTF(input);
                out.flush();
                if (input.equals("-")) break;
            }

            scanner.close();
            out.close();
            in.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}