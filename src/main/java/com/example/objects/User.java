package com.example.objects;

import java.io.DataOutputStream;

public class User {

    private final String name;
    private final String username;
    private final String password;
    private DataOutputStream stream;

    public User(String username, String password) {
        this.name     = username;
        this.username = username;
        this.password = password;
    }

    public User(String name, String username, String password) {
        this.name     = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public DataOutputStream getStream() {
        return stream;
    }

    public void setStream(DataOutputStream stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', username='" + username + "'}";
    }
}