package com.artqqwr.socketchat.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.artqqwr.socketchat.message.CommandMessage;
import com.artqqwr.socketchat.message.ErrorMessage;
import com.artqqwr.socketchat.message.Message;
import com.artqqwr.socketchat.message.MessageSerializer;
import com.artqqwr.socketchat.message.NotificationMessage;
import com.artqqwr.socketchat.message.TextMessage;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String username;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.printf("Connected to server %s:%d\n", host, port);

            new Thread(() -> {
                String json;
                try {
                    while ((json = in.readLine()) != null) {
                        Message msg = MessageSerializer.deserialize(json);
                        switch (msg) {
                            case TextMessage textMsg ->
                                System.out.printf("[%s]: %s\n", textMsg.getSender(), textMsg.getText());
                            case CommandMessage cmdMsg ->
                                System.out.printf("SERVER: %s %s\n", cmdMsg.getSender(), cmdMsg.getArgument());
                            case ErrorMessage errorMsg ->
                                System.out.printf("ERROR from %s: %s\n", errorMsg.getSender(), errorMsg.getError());
                            case NotificationMessage notificationMsg ->
                                System.out.printf("INFO from %s: %s\n", notificationMsg.getSender(),
                                        notificationMsg.getNotification());
                            default ->
                                System.out.println("Unknown message: " + json);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage(Message message) {
        if (out == null)
            return;

        String json = MessageSerializer.serialize(message);
        out.println(json);
    }

    public void sendTextMessage(String text) {
        sendMessage(new TextMessage(this.username, text));
    }

    public void sendCommandMessage(String command, String password, String argument) {
        sendMessage(new CommandMessage(this.username, command, password, argument));
    }

    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
