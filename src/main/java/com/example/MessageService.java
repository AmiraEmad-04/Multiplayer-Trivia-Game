package com.example;

import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private static final List<String> sender = new ArrayList<>();
    private static final List<String> reciever = new ArrayList<>();
    private static final List<String> messages = new ArrayList<>();

    public synchronized String saveMessage(String senderVal, String receiverVal, String messageVal) {
        int count = 0;
        for (int i = 0; i < reciever.size(); i++) {
            if (reciever.get(i).equals(receiverVal)) {
                count++;
            }
        }

        if (count < 10) {
            sender.add(senderVal);
            reciever.add(receiverVal);
            messages.add(messageVal);
            System.out.println("Saved: " + senderVal + " -> " + receiverVal + ": " + messageVal);
            return "OK";
        } else {
            return "MAX";
        }
    }

    public synchronized List<String> readMessages(String receiverVal) {
        List<String> result = new ArrayList<>();
        for (int i = reciever.size() - 1; i >= 0; i--) {
            if (reciever.get(i).equals(receiverVal)) {
                result.add(sender.get(i) + ": " + messages.get(i));
                reciever.remove(i);
                sender.remove(i);
                messages.remove(i);
            }
        }
        return result;
    }
}