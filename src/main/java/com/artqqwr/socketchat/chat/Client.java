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

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server " + host + ":" + port);

            new Thread(() -> {
                String json;
                try {
                    while ((json = in.readLine()) != null) {
                        Message msg = MessageSerializer.deserialize(json);
                        if (msg instanceof TextMessage) {
                            System.out.println("[" + msg.getSender() + "]: " + ((TextMessage) msg).getText());
                        } else if (msg instanceof CommandMessage) {
                            CommandMessage cmdMsg = (CommandMessage) msg;
                            System.out.println("SERVER: " + msg.getSender() + " " + cmdMsg.getArgument());
                        } else if (msg instanceof ErrorMessage) {
                            ErrorMessage errMsg = (ErrorMessage) msg;
                            System.out.println("ERROR from " + msg.getSender() + ": " + errMsg.getError());
                        } else if (msg instanceof NotificationMessage) {
                            NotificationMessage notificationMessage = (NotificationMessage) msg;
                            System.out.println("INFO from " + msg.getSender() + ": " + notificationMessage.getNotification());
                        } else {
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

    public void sendMessage(Message message) {
        if (out != null) {
            var json = MessageSerializer.serialize(message);
            out.println(json);
        }
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
